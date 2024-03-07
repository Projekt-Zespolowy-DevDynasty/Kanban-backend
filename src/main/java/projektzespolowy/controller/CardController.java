package projektzespolowy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import projektzespolowy.models.Card;
import projektzespolowy.models.Task;
import projektzespolowy.repository.CardRepository;
import projektzespolowy.repository.TaskRepository;
import projektzespolowy.wyjatki.ResourceNotFoundException;

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

    @GetMapping("/all")
    private List<Card> getAllCards(){
        return cardRepository.findAll();
    }
    @PostMapping("/add")
    private Card addCard(@RequestBody Card card){
        return cardRepository.save(card);
    }
    @PutMapping("/addtask/{id}")
    private Card addTask(@RequestBody String taskName, @PathVariable Long id) {
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
}

