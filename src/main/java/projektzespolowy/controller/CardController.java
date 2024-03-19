package projektzespolowy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projektzespolowy.models.Card;
import projektzespolowy.models.Task;
import projektzespolowy.repository.CardRepository;
import projektzespolowy.repository.TaskRepository;
import projektzespolowy.wyjatki.ResourceNotFoundException;


import java.util.*;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/card")
public class CardController {
    private CardRepository cardRepository;
    private final TaskRepository taskRepository;

    @Autowired
    public CardController(CardRepository cardRepository,
                          TaskRepository taskRepository) {
        this.cardRepository = cardRepository;
        this.taskRepository = taskRepository;
    }

    @GetMapping("/{id}")
    private Card getCard(@PathVariable Long id) {
        return cardRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono karty o podanym ID: " + id));
    }
    @GetMapping("/all")
    private List<Card> getAllCards() {
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
        cardRepository.save(card);
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


        Card leftCard = cardRepository.findFirstByIdLessThanOrderByIdDesc(cardId);

        if (leftCard != null) {

            List<Task> tasksToMove = cardToDelete.getTasks();
            leftCard.getTasks().addAll(tasksToMove);
            cardRepository.save(leftCard);
        }


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
    @PutMapping("/{id}/position")
    public void updateCardPosition(@PathVariable Long id, @RequestBody int position) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono karty o podanym ID: " + id));

        card.setPosition(position);
        cardRepository.save(card);


        List<Card> cards = cardRepository.findAll();
        for (int i = 0; i < cards.size(); i++) {
            cards.get(i).setPosition(i);
        }
        cardRepository.saveAll(cards);
    }


}