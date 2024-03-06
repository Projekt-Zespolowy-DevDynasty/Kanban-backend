package projektzespolowy.controller;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import projektzespolowy.models.Card;
import projektzespolowy.models.Task;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api")

public class testowy {
    @GetMapping("/test")
    public List<Card> test(){
        Card kartaToDo = new Card();
        Task task = new Task(1L, "dodac przycisk");
        Task task2 = new Task(2L, "zmienic kolor tla");
        Task task3 = new Task(3L, "cos zrobic ");
        kartaToDo.setTasks(List.of(task, task2, task3));
        kartaToDo.setName("To Do");
        kartaToDo.setId(1L);
        Card kartaInProgress = new Card();
        kartaInProgress.setName("In Progress");
        kartaInProgress.setTasks(List.of(task));
        Card kartaDone = new Card();
        kartaDone.setName("Done");
        Card kartaJakasinna = new Card();
        kartaJakasinna.setName("Jakas inna");

        return List.of(kartaToDo, kartaInProgress, kartaDone, kartaJakasinna);
    }


}
