package projektzespolowy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projektzespolowy.models.Card;
import projektzespolowy.models.Task;
import projektzespolowy.repository.CardRepository;
import projektzespolowy.repository.TaskRepository;
import projektzespolowy.service.CardServiceImpl;
import projektzespolowy.wyjatki.ResourceNotFoundException;


import java.util.*;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/card")
public class CardController {
    private CardRepository cardRepository;
    private final TaskRepository taskRepository;
    private CardServiceImpl cardService;

    @Autowired
    public CardController(CardRepository cardRepository,
                          TaskRepository taskRepository,
                          CardServiceImpl cardService) {
        this.cardService = cardService;
        this.cardRepository = cardRepository;
        this.taskRepository = taskRepository;
    }

    @GetMapping("/{id}")
    private Card getCard(@PathVariable Long id) {
        return cardRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono karty o podanym ID: " + id));
    }
    @GetMapping("/all")
    private List<Card> getAllCards() {
        return cardService.getAllCards();
    }

    @PostMapping("/add")
    private Card addCard(@RequestBody Card card) {
        if(card.getName().equals("To do") || card.getName().equals("Done")){
            throw new UnsupportedOperationException("Nie można dodać karty o nazwie: " + card.getName());
        }
        if (card.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Nazwa karty nie może być pusta ani składać się wyłącznie z białych znaków.");
        }
        List<Card> cards = cardRepository.findAll();
        // get Done card from cards
        Card doneCard2 = cards.stream()
                .filter(c -> c.getName().equals("Done"))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono karty o nazwie: Done"));
        Card doneCard = cardRepository.findById(doneCard2.getId()).orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono karty o nazwie: Done"));
        card.setPosition(doneCard.getPosition());
        cardRepository.save(doneCard);
        doneCard.setPosition(doneCard.getPosition() + 1);

        card.setMaxTasksLimit(5);
        return cardRepository.save(card);
    }






    @PutMapping("/addtask/{id}")
    private Card addTask(@RequestBody String taskName, @PathVariable Long id) {
        if (taskName.trim().isEmpty()) {
            throw new IllegalArgumentException("Nazwa zadania nie może być pusta ani składać się wyłącznie z białych znaków.");
        }
        Card card = cardRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Nie znaleziono karty o podanym ID: " + id));
        Task newTask = new Task();
        newTask.setName(taskName);
        taskRepository.save(newTask);
        List<Task> tasks = card.getTasks();
        tasks.add(newTask);
        card.setTasks(tasks);
        return cardRepository.save(card);
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

    @PutMapping("/{sourceCardId}/move-task/{taskId}/to-card/{destinationCardId}")
    private void moveTaskToAnotherCard(@PathVariable Long sourceCardId, @PathVariable Long taskId, @PathVariable Long destinationCardId) {

        Card sourceCard = cardRepository.findById(sourceCardId)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono karty numer: " + sourceCardId));
        Card destinationCard = cardRepository.findById(destinationCardId)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono karty numer: " + destinationCardId));

        Task taskToMove = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono zadania numer: " + taskId));

        sourceCard.getTasks().remove(taskToMove);
        destinationCard.getTasks().add(taskToMove);

        cardRepository.save(sourceCard);
        cardRepository.save(destinationCard);
    }
    @PutMapping("/{id}/maxTasksLimit")
    public void updateMaxTasksLimit(@PathVariable Long id, @RequestBody int maxTasksLimit) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono karty o podanym ID: " + id));

        card.setMaxTasksLimit(maxTasksLimit);
        cardRepository.save(card);
    }


    @DeleteMapping("/{cardId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCardAndMoveTasks(@PathVariable Long cardId) {
        Card cardToDelete = cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono karty numer: " + cardId));

        int position = cardToDelete.getPosition();
        if (cardToDelete.getName().equals("To do") || cardToDelete.getName().equals("Done")) {
            throw new UnsupportedOperationException("Nie można usunąć tej karty.");
        }

        Card leftCard = cardRepository.findByPosition(position - 1)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono karty o pozycji: " + (position - 1)));

        List<Card> cards = cardRepository.findAllByPositionGreaterThan(position);
        for (Card card : cards) {
            card.setPosition(card.getPosition() - 1);
            cardRepository.save(card);
        }
        // TODO: nie tworzyc nowych obiektow, tylko przypisac do nowej karty te same zadania
        List<Task> tasks = new ArrayList<>();

        for (Task task : cardToDelete.getTasks()) {
            Task newTask = new Task();
            newTask.setName(task.getName());
            taskRepository.save(newTask);
            tasks.add(newTask);
        }
        List<Task> leftCardTasks = leftCard.getTasks();
        leftCardTasks.addAll(tasks);


        cardRepository.save(leftCard);
        cardRepository.delete(cardToDelete);
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
    public void updateCardPosition(@PathVariable Long destinationId, @PathVariable Long sourceId) {
        Card destinationCard = cardRepository.findById(destinationId)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono karty o podanym ID: " + destinationId));
        Card sourceCard = cardRepository.findById(sourceId)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono karty o podanym ID: " + sourceId));
        int destinationPostion = destinationCard.getPosition();
        int sourcePosition = sourceCard.getPosition();

        if (destinationPostion < sourcePosition) {
            List<Card> cards = cardRepository.findAllByPositionGreaterThanAndPositionLessThan(destinationCard.getPosition(), sourceCard.getPosition());

            for (Card card : cards) {
                card.setPosition(card.getPosition() + 1);
                cardRepository.save(card);
            }
            sourceCard.setPosition(destinationCard.getPosition() + 1);
            cardRepository.save(sourceCard);

        } else if (destinationCard.getPosition() > sourceCard.getPosition()) {
            List<Card> cards = cardRepository.findAllByPositionGreaterThanEqualAndPositionLessThan(sourceCard.getPosition(), destinationCard.getPosition());

            for (Card card : cards) {
                card.setPosition(card.getPosition() - 1);
                cardRepository.save(card);
            }
            sourceCard.setPosition(destinationCard.getPosition());
            cardRepository.save(sourceCard);
            destinationCard.setPosition(destinationCard.getPosition() - 1);
            cardRepository.save(destinationCard);


        }
    }
}