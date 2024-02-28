package projektzespolowy.models;

import jakarta.persistence.*;

import java.util.List;

//TODO: wszystkie adnotacje
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;

    // TODO: relacja 1:n
    private List<Task> tasks;

}
