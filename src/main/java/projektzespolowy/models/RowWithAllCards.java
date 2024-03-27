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
public class RowWithAllCards {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "id", nullable = false)

    private Long id;
    private int position;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Card> cardsinrow;
}
