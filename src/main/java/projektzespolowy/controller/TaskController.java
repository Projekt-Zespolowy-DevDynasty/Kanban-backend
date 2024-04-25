package projektzespolowy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projektzespolowy.DTO.CardDTO;
import projektzespolowy.DTO.TaskDTO;
import projektzespolowy.service.TasksService;

import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TasksService tasksService;

    @GetMapping("/tasks")
    public ResponseEntity<List<TaskDTO>> getAllTasks() {
        List<TaskDTO> tasks = tasksService.getAllTasks();
        return new ResponseEntity<>(tasks, HttpStatus.OK);
    }

    @PutMapping("/task/add/{id}")
    public ResponseEntity<CardDTO> addTaskToCard(@RequestBody String taskName, @PathVariable Long cardId) {
        CardDTO cardDTO = tasksService.addTask(taskName, cardId);
        return new ResponseEntity<>(cardDTO, HttpStatus.CREATED);
    }

    @PutMapping("/task/{taskId}/rename")
    public ResponseEntity<TaskDTO> renameTask(@PathVariable Long taskId, @RequestBody String newName) {
        TaskDTO taskDTO = tasksService.renameTask(taskId, newName);
        return new ResponseEntity<>(taskDTO, HttpStatus.OK);
    }

    @PutMapping("/task/changecolor/{taskId}")
    public ResponseEntity<TaskDTO> changeTaskColor(@PathVariable Long taskId, @RequestBody String color) {
        TaskDTO taskDTO = tasksService.changeTaskColor(taskId, color);
        return new ResponseEntity<>(taskDTO, HttpStatus.OK);
    }
}
