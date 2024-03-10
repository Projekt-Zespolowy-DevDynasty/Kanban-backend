package projektzespolowy.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import projektzespolowy.models.Card;
import projektzespolowy.models.Task;
import projektzespolowy.repository.CardRepository;
import projektzespolowy.repository.TaskRepository;
import projektzespolowy.wyjatki.ResourceNotFoundException;

import java.util.List;

@Service
public class CardService {

    private final CardRepository cardRepository;
    private final TaskRepository taskRepository;

    @Autowired
    public CardService(CardRepository cardRepository, TaskRepository taskRepository) {
        this.cardRepository = cardRepository;
        this.taskRepository = taskRepository;
    }

    public List<Card> getAllCards() {
        return cardRepository.findAll();
    }

    public Card addTaskToCard(Long cardId, String taskName) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono karty o podanym ID: " + cardId));

        if (card.getTaskNumber() >= card.getMaxTasksLimit()) {
            throw new IllegalArgumentException("Przekroczono maksymalny limit zadań na karcie.");
        }

        Task newTask = new Task();
        newTask.setName(taskName);
        taskRepository.save(newTask);

        List<Task> tasks = card.getTasks();
        tasks.add(newTask);
        card.setTasks(tasks);
        card.setTaskNumber(card.getTaskNumber() + 1);

        return cardRepository.save(card);
    }

    public void deleteTaskFromCard(Long cardId, Long taskId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono karty numer: " + cardId));

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono zadania numer: " + taskId));

        card.getTasks().remove(task);
        card.setTaskNumber(card.getTaskNumber() - 1);
        cardRepository.save(card);
    }

    public void moveTaskToAnotherCard(Long sourceCardId, Long taskId, Long destinationCardId) {

    }
}
