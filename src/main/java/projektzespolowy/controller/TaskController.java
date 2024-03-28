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

@CrossOrigin(origins = "https://devdynastykanban.azurewebsites.net/")
@RestController
@RequestMapping("/api")
public class TaskController {
//
    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CardRepository cardRepository;

    @GetMapping("/tasks")
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
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