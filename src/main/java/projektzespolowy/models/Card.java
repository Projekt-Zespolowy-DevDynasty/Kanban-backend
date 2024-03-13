package projektzespolowy.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)
    private Long id;
    private String name;
    private int maxTasksLimit;
    private int TaskNumber;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Task> tasks;

    public Card(String name) {
        this.name = name;
    }

}