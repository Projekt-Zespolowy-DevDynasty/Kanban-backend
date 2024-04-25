package projektzespolowy.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import projektzespolowy.DTO.CardDTO;
import projektzespolowy.models.Card;
import projektzespolowy.models.RowWithAllCards;
import projektzespolowy.models.Task;
import projektzespolowy.repository.CardRepository;
import projektzespolowy.repository.RowRepository;
import projektzespolowy.repository.TaskRepository;
import projektzespolowy.wyjatki.ResourceNotFoundException;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class CardServiceImpl implements CardService {
    private CardRepository cardRepository;
    private TaskRepository taskRepository;
    private RowRepository rowWithAllCardsRepository;
    @Autowired
    public CardServiceImpl(CardRepository cardRepository, TaskRepository taskRepository, RowRepository rowWithAllCardsRepository) {
        this.cardRepository = cardRepository;
        this.taskRepository = taskRepository;
        this.rowWithAllCardsRepository = rowWithAllCardsRepository;
    }
    public Card getCard(Long id) {
        return cardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono karty o podanym ID: " + id));
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
    public Card addCard(CardDTO cardDTO) {
        // Walidacja danych w cardDTO
        if (cardDTO.getName() == null || cardDTO.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Nazwa karty nie może być pusta ani składać się wyłącznie z białych znaków.");
        }

        if (cardDTO.getName().equals("To do") || cardDTO.getName().equals("Done")) {
            throw new UnsupportedOperationException("Nie można dodać karty o nazwie: " + cardDTO.getName());
        }

        // Znalezienie karty "Done" w bazie danych
        Card doneCard = cardRepository.findByName("Done").orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono karty o nazwie: Done"));

        // Tworzenie nowej karty na podstawie danych z cardDTO
        Card card = new Card();
        card.setName(cardDTO.getName());
        card.setMaxTasksLimit(cardDTO.getMaxTasksLimit());
        card.setPosition(doneCard.getPosition());
        doneCard.setPosition(doneCard.getPosition() + 1);
        card.setMaxTasksLimit(5); // Możesz zmienić limit zadań, jeśli jest to konieczne

        // Inicjalizacja pustej listy zadań dla nowej karty
        card.setTasks(new ArrayList<>());

        // Zapisanie zmian w karcie "Done"
        cardRepository.save(doneCard);

        // Zapisanie nowej karty do repozytorium
        Card savedCard = cardRepository.save(card);
        return savedCard;
    }


    public void deleteTaskFromCard(Long cardId, Long taskId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono karty numer: " + cardId));

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono zadania numer: " + taskId));

        card.getTasks().remove(task);
        cardRepository.save(card);
    }

    public void updateMaxTasksLimit(Long id, int maxTasksLimit) {
        Card targetCard = cardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono karty o podanym ID: " + id));

        int targetPosition = targetCard.getPosition();

        List<RowWithAllCards> rows = rowWithAllCardsRepository.findAll();

        rows.forEach(row -> {
            row.getCardsinrow().forEach(card -> {
                if (card.getPosition() == targetPosition) {
                    card.setMaxTasksLimit(maxTasksLimit);
                }
            });
        });

        // Po aktualizacji wartości maxTasksLimit dla wszystkich kart, zapisz zmiany w bazie danych
        cardRepository.saveAll(rows.stream().flatMap(row -> row.getCardsinrow().stream()).collect(Collectors.toList()));
    }


    public void editColumnName(Long id, String newName) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono karty o podanym ID: " + id));

        if (newName.trim().isEmpty() || newName.equals("To do") || newName.equals("Done")) {
            throw new IllegalArgumentException("Nazwa kolumny nie może być pusta ani składać się wyłącznie z białych znaków.");
        }
        card.setName(newName);
        cardRepository.save(card);
    }

    public void updateCardPosition(Long destinationId, Long sourceId) {
        Card destinationCard = cardRepository.findById(destinationId)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono karty o podanym ID: " + destinationId));
        Card sourceCard = cardRepository.findById(sourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono karty o podanym ID: " + sourceId));

        int destinationPosition = destinationCard.getPosition();
        int sourcePosition = sourceCard.getPosition();

        if (destinationPosition < sourcePosition) {
            List<Card> cards = cardRepository.findAllByPositionGreaterThanAndPositionLessThan(destinationPosition, sourcePosition);

            for (Card card : cards) {
                card.setPosition(card.getPosition() + 1);
                cardRepository.save(card);
            }
            sourceCard.setPosition(destinationPosition + 1);
            cardRepository.save(sourceCard);
        } else if (destinationPosition > sourcePosition) {
            List<Card> cards = cardRepository.findAllByPositionGreaterThanEqualAndPositionLessThan(sourcePosition, destinationPosition);

            for (Card card : cards) {
                card.setPosition(card.getPosition() - 1);
                cardRepository.save(card);
            }
            sourceCard.setPosition(destinationPosition);
            cardRepository.save(sourceCard);
            destinationCard.setPosition(destinationPosition - 1);
            cardRepository.save(destinationCard);
        }
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
