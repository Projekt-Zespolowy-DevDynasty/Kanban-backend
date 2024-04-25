package projektzespolowy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projektzespolowy.DTO.SubTasksDTO;
import projektzespolowy.service.SubTasksService;

import java.util.List;

@RestController
@RequestMapping("api/subtasks")
public class SubTasksController {

    private final SubTasksService subTasksService;

    @Autowired
    public SubTasksController(SubTasksService subTasksService) {
        this.subTasksService = subTasksService;
    }

    @PostMapping("/{taskId}")
    public ResponseEntity<SubTasksDTO> createSubTask(@PathVariable Long taskId, @RequestBody String name) {

        SubTasksDTO createdSubTask = subTasksService.createSubTask(taskId, name);
        if (createdSubTask != null) {
            return new ResponseEntity<>(createdSubTask, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{subTaskId}")
    public ResponseEntity<Void> deleteSubTask(@PathVariable Long subTaskId) {
        subTasksService.deleteSubTask(subTaskId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    @GetMapping("/{taskId}/percentageFinishedSubTasks")
    public ResponseEntity<Double> getPercentageFinishedSubTasks(@PathVariable Long taskId) {
        double percentage = subTasksService.calculatePercentageOfFinishedSubTasks(taskId);
        return ResponseEntity.ok(percentage);
    }


    @PutMapping("/{subTaskId}/finished")
    public ResponseEntity<SubTasksDTO> updateSubTaskFinished(@PathVariable Long subTaskId, @RequestParam String finished) {
        boolean finishedBool = Boolean.parseBoolean(finished);
        SubTasksDTO updatedSubTask = subTasksService.updateSubTaskFinished(subTaskId, finishedBool);
        if (updatedSubTask != null) {
            return new ResponseEntity<>(updatedSubTask, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/{subTaskId}/name")
    public ResponseEntity<SubTasksDTO> updateSubTaskName(@PathVariable Long subTaskId, @RequestBody String Name) {
        SubTasksDTO updatedSubTask = subTasksService.updateSubTaskName(subTaskId, Name);
        if (updatedSubTask != null) {
            return new ResponseEntity<>(updatedSubTask, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    @GetMapping("/task/{taskId}/finished-count")
    public ResponseEntity<Integer> countFinishedSubTasks(@PathVariable Long taskId) {
        int count = subTasksService.countFinishedSubTasks(taskId);
        return new ResponseEntity<>(count, HttpStatus.OK);
    }

    @GetMapping("/task/{taskId}/subtasks-count")
    public ResponseEntity<Integer> countAllSubTasks(@PathVariable Long taskId) {
        int count = subTasksService.countAllSubTasks(taskId);
        return new ResponseEntity<>(count, HttpStatus.OK);
    }
    @GetMapping("/tasks/{taskId}/subtasks")
    public ResponseEntity<List<SubTasksDTO>> getSubTasksByTaskId(@PathVariable Long taskId) {
        List<SubTasksDTO> subTasks = subTasksService.getSubTasksByTaskId(taskId);
        return new ResponseEntity<>(subTasks, HttpStatus.OK);
    }
}
