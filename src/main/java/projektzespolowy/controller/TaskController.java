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
    @PutMapping("/addtask/{id}")
    private Card addTask(@RequestBody String taskName, @PathVariable Long id) {
        if (taskName.trim().isEmpty()) {
            throw new IllegalArgumentException("Nazwa zadania nie może być pusta ani składać się wyłącznie z białych znaków.");
        }
        Card card = cardRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Nie znaleziono karty o podanym ID: " + id));
        Task newTask = new Task();
        newTask.setName(taskName);
        newTask.setColor("#f0f0f0");
        newTask.setMaxUserLimit(5);
        taskRepository.save(newTask);
        List<Task> tasks = card.getTasks();
        tasks.add(newTask);
        card.setTasks(tasks);
        return cardRepository.save(card);
    }
    @PutMapping("/task/{taskId}/changeUserLimit")
    public Task changeTaskUserLimit(@PathVariable Long taskId, @RequestParam int newLimit) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono zadania o podanym ID: " + taskId));

        if (newLimit == -1) {
            task.setMaxUserLimit(Integer.MAX_VALUE);
        } else if (newLimit < 1) {
            throw new IllegalArgumentException("Limit użytkowników musi być większy niż 0.");
        } else {
            task.setMaxUserLimit(newLimit);
        }

        return taskRepository.save(task);
    }

    @PutMapping("/task/changecolor/{taskId}/{color}")
    public Task changeTaskColor(@PathVariable Long taskId, @PathVariable String color) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono zadania o podanym ID: " + taskId));
        task.setColor(color);

        return taskRepository.save(task);
    }



}
