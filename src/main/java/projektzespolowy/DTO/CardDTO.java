package projektzespolowy.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import projektzespolowy.models.Card;
import projektzespolowy.models.Task;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CardDTO {
    private Long id;
    private String name;
    private int maxTasksLimit;
    private int taskNumber;
    private int position;

    private List<Long> taskIds;


    public static CardDTO from(Card card) {
        CardDTO dto = new CardDTO();
        dto.setId(card.getId());
        dto.setName(card.getName());
        dto.setMaxTasksLimit(card.getMaxTasksLimit());
        dto.setTaskNumber(card.getTaskNumber());
        dto.setPosition(card.getPosition());

        if (card.getTasks() != null) {
            List<Long> taskIds = card.getTasks().stream()
                    .map(Task::getId)
                    .collect(Collectors.toList());
            dto.setTaskIds(taskIds);
        }
        return dto;
    }
}
