package projektzespolowy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import projektzespolowy.models.Board;
import projektzespolowy.models.Task;
import projektzespolowy.repository.TaskRepository;
import projektzespolowy.wyjatki.ResourceNotFoundException;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskRepository taskRepository;

    @GetMapping
    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Task createTask(@RequestBody Task task) {
        return taskRepository.save(task);
    }

//    @PutMapping("/{id}")
//    public Task updateTask(@PathVariable Long id, @RequestBody Task taskDetails) {
//        Task task = taskRepository.findById(id)
//                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono zadania numer: " + id));
//
//        task.setTitle(taskDetails.getTitle());
//        task.setDescription(taskDetails.getDescription());
//        task.setStatus(taskDetails.getStatus());
//
//        return taskRepository.save(task);
//    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono zadania numer: " + id));

        taskRepository.delete(task);
    }
}


