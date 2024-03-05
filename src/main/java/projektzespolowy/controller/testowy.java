package projektzespolowy.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import projektzespolowy.models.Card;
import projektzespolowy.models.Task;

import java.util.List;

@RestController
@RequestMapping("/api")
public class testowy {
    @GetMapping("/test")
    public List<Card> test(){
        Card kartaToDo = new Card();
        Task task = new Task(1L, "task1");
        Task task2 = new Task(2L, "task2");
        Task task3 = new Task(3L, "task3");
        kartaToDo.setTasks(List.of(task, task2, task3));
        kartaToDo.setName("To Do");
        Card kartaInProgress = new Card();
        kartaInProgress.setName("In Progress");
        Card kartaDone = new Card();
        kartaDone.setName("Done");


        return List.of(kartaToDo, kartaInProgress, kartaDone);
    }


}
