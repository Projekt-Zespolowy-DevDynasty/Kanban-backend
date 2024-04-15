package projektzespolowy.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projektzespolowy.DTO.CardDTO;
import projektzespolowy.models.Card;
import projektzespolowy.models.Task;
import projektzespolowy.models.RowWithAllCards;
import projektzespolowy.repository.CardRepository;
import projektzespolowy.repository.RowRepository;
import projektzespolowy.repository.TaskRepository;
import projektzespolowy.service.CardServiceImpl;
import projektzespolowy.wyjatki.ResourceNotFoundException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/card")
public class CardController {
    private final CardRepository cardRepository;
    private final TaskRepository taskRepository;
    private final CardServiceImpl cardService;
    private final RowRepository rowWithAllCardsRepository;

    @Autowired
    public CardController(CardRepository cardRepository,
                          TaskRepository taskRepository,
                          CardServiceImpl cardService, RowRepository rowWithAllCardsRepository) {
        this.cardService = cardService;
        this.cardRepository = cardRepository;
        this.taskRepository = taskRepository;
        this.rowWithAllCardsRepository = rowWithAllCardsRepository;
    }

    @GetMapping("/{id}")
    private ResponseEntity<CardDTO> getCard(@PathVariable Long id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono karty o podanym ID: " + id));
        return ResponseEntity.ok(CardDTO.from(card));
    }


    @Operation(summary = "Zwraca wszystkie Karty")
    @GetMapping("/all")
    private ResponseEntity<List<CardDTO>> getAllCards() {
        List<Card> cards = cardService.getAllCards();
        List<CardDTO> cardDTOs = cards.stream()
                .map(CardDTO::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(cardDTOs);
    }


    @PostMapping("/add")
    private ResponseEntity<CardDTO> addCard(@RequestBody CardDTO cardDTO) {
        if (cardDTO.getName() == null || cardDTO.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Nazwa karty nie może być pusta ani składać się wyłącznie z białych znaków.");
        }

        if (cardDTO.getName().equals("To do") || cardDTO.getName().equals("Done")) {
            throw new UnsupportedOperationException("Nie można dodać karty o nazwie: " + cardDTO.getName());
        }

        List<Card> cards = cardRepository.findAll();
        Card doneCard = cards.stream()
                .filter(c -> c.getName().equals("Done"))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono karty o nazwie: Done"));

        Card card = new Card();
        card.setName(cardDTO.getName());
        card.setMaxTasksLimit(cardDTO.getMaxTasksLimit());
        card.setPosition(doneCard.getPosition());
        doneCard.setPosition(doneCard.getPosition() + 1);
        card.setMaxTasksLimit(5);

        cardRepository.save(doneCard);
        Card savedCard = cardRepository.save(card);
        return new ResponseEntity<>(CardDTO.from(savedCard), HttpStatus.CREATED);
    }


    @DeleteMapping("/{cardId}/task/{taskId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTaskFromCard(@PathVariable Long cardId, @PathVariable Long taskId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono karty numer: " + cardId));

        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono zadania numer: " + taskId));

        card.getTasks().remove(task);
        cardRepository.save(card);
    }

    @PutMapping("/{id}/maxTasksLimit")
    public ResponseEntity<Void> updateMaxTasksLimit(@PathVariable Long id, @RequestBody int maxTasksLimit) {
        Card targetCard = cardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono karty o podanym ID: " + id));


        int targetPosition = targetCard.getPosition();


        List<RowWithAllCards> rows = rowWithAllCardsRepository.findAll();

        rows.forEach(row -> {
            row.getCardsinrow().forEach(card -> {
                if (card.getPosition() == targetPosition) {
                    card.setMaxTasksLimit(maxTasksLimit);
                    cardRepository.save(card);
                }
            });
        });

        return ResponseEntity.noContent().build();
    }
    @PutMapping("/{id}/edit-name")
    public ResponseEntity<Map<String, String>> editColumnName(@PathVariable Long id, @RequestBody String newName) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono karty o podanym ID: " + id));

        if (newName.trim().isEmpty() || newName.equals("To do") || newName.equals("Done")) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Nazwa kolumny nie może być pusta ani składać się wyłącznie z białych znaków.");
            return ResponseEntity.badRequest().body(response);
        }

        card.setName(newName);
        cardRepository.save(card);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Nazwa kolumny została pomyślnie zaktualizowana.");

        return ResponseEntity.ok(response);
    }
    @PutMapping("/{destinationId}/position/{sourceId}")
    public ResponseEntity<Void> updateCardPosition(@PathVariable Long destinationId, @PathVariable Long sourceId) {
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

        return ResponseEntity.noContent().build();
    }
    @PutMapping("/{sourceCardId}/move-task/{taskId}/to-card/{destinationCardId}/at-index/{index}")
    private void moveTaskToAnotherCard(@PathVariable Long sourceCardId, @PathVariable Long taskId,
                                       @PathVariable Long destinationCardId, @PathVariable int index) {
        cardService.moveTask(sourceCardId, taskId, destinationCardId, index);
    }


}
