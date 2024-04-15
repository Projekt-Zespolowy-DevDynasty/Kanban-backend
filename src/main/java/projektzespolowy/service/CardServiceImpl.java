package projektzespolowy.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import projektzespolowy.models.Card;
import projektzespolowy.models.Task;
import projektzespolowy.repository.CardRepository;
import projektzespolowy.repository.TaskRepository;
import projektzespolowy.wyjatki.ResourceNotFoundException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

@Service
public class CardServiceImpl implements CardService {
    private CardRepository cardRepository;
    private TaskRepository taskRepository;
    @Autowired
    public CardServiceImpl(CardRepository cardRepository, TaskRepository taskRepository) {
        this.cardRepository = cardRepository;
        this.taskRepository = taskRepository;
    }

    @Override
    public List<Card> getAllCards() {
        List<Card> karty = cardRepository.findAll();
        Card kartaToDo = cardRepository.findByName("To do").orElseGet(() -> cardRepository.save(new Card("To do", Integer.MAX_VALUE, 0)));
        Card kartaDone = cardRepository.findByName("Done").orElseGet(() -> cardRepository.save(new Card("Done", Integer.MAX_VALUE, 1)));

        List<Card> pomocniczaLista = new ArrayList<>();
        pomocniczaLista.add(kartaToDo);
        for (Card card : karty) {
            if (!card.getName().equals("To do") && !card.getName().equals("Done")) {
                pomocniczaLista.add(card);
            }
        }
        pomocniczaLista.add(kartaDone);


        pomocniczaLista.sort(Comparator.comparingInt(Card::getPosition));

        return pomocniczaLista;
    }
    @Transactional
    public void moveTask(Long sourceCardId, Long taskId,
                         Long destinationCardId, int index){
        Card sourceCard = cardRepository.findById(sourceCardId)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono karty numer: " + sourceCardId));

        Card destinationCard = cardRepository.findById(destinationCardId)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono karty numer: " + destinationCardId));

        Task taskToMove = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono zadania numer: " + taskId));


        sourceCard.getTasks().remove(taskToMove);

        // Dodaj zadanie do karty docelowej
        List<Task> destinationTasks = destinationCard.getTasks();
        destinationTasks.sort(Comparator.comparingInt(Task::getPosition));
        destinationTasks.add(index, taskToMove);

        // Uaktualnienie pozycji dla wszystkich zadań na karcie docelowej
        IntStream.range(0, destinationTasks.size())
                .forEach(i -> destinationTasks.get(i).setPosition(i));
        destinationCard.setTasks(destinationTasks);

        taskRepository.saveAll(destinationCard.getTasks());
        cardRepository.save(sourceCard);
        cardRepository.save(destinationCard);
    }

}
