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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api")
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CardRepository cardRepository;

    @GetMapping("/tasks")
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }





    @GetMapping("/cards")
    public List<Card> getAllCards() {
        return cardRepository.findAll();
    }

    @PostMapping("/cards")
    @ResponseStatus(HttpStatus.CREATED)
    public Card createCard(@RequestBody Card card) {
        card.setMaxTasksLimit(5);
        card.setTaskNumber(0);
        return cardRepository.save(card);
    }

    @DeleteMapping("/cards/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCard(@PathVariable Long id) {

        Card card = cardRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono karty numer: " + id));
       if (card.getName().equals("To do") || (card.getName().equals("Done"))) {
            throw new UnsupportedOperationException("Nie można usunąć tej karty.");
        }
        cardRepository.delete(card);
    }
    @PutMapping("/task/{taskId}/rename")
    public ResponseEntity<Map<String, String>> renameTask(@PathVariable Long taskId, @RequestBody String newName) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono zadania o podanym ID: " + taskId));

        if (newName.trim().isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("message", "Nazwa zadania nie może być pusta ani składać się wyłącznie z białych znaków.");
            return ResponseEntity.badRequest().body(response);
        }

        task.setName(newName);
        taskRepository.save(task);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Nazwa zadania została pomyślnie zaktualizowana.");

        return ResponseEntity.ok(response);
    }
}
//Truskawka