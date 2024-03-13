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



import java.util.ArrayList;
import java.util.List;

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
        Card kartaToDo = cardRepository.findByName("To do").orElseGet(() -> cardRepository.save(new Card("To do")));
        Card kartaDone = cardRepository.findByName("Done").orElseGet(() -> cardRepository.save(new Card("Done")));
        List<Card> pomocniczaLista = new ArrayList<>();
        pomocniczaLista.add(kartaToDo);
        for (Card card : karty) {
            if (!card.getName().equals("To do") && !card.getName().equals("Done")) {
                pomocniczaLista.add(card);
            }
        }
        pomocniczaLista.add(kartaDone);

        return pomocniczaLista;
    }

    @PostMapping("/add")
    private Card addCard(@RequestBody Card card) {
        if (card.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Nazwa karty nie może być pusta ani składać się wyłącznie z białych znaków.");
        }

        if (card.getName().equalsIgnoreCase("To do")) {
            card.setMaxTasksLimit(Integer.MAX_VALUE);
        } else if (card.getName().equalsIgnoreCase("Done")) {
            card.setMaxTasksLimit(Integer.MAX_VALUE);
        } else {
            card.setMaxTasksLimit(5);
        }

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

}