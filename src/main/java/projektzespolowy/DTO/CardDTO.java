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

    private List<TaskDTO> tasks;

    public static CardDTO from(Card card) {
        CardDTO dto = new CardDTO();
        dto.setId(card.getId());
        dto.setName(card.getName());
        dto.setMaxTasksLimit(card.getMaxTasksLimit());
        dto.setTaskNumber(card.getTaskNumber());
        dto.setPosition(card.getPosition());

        if (card.getTasks() != null) {
            List<TaskDTO> taskDTOs = card.getTasks().stream()
                    .map(TaskDTO::from)
                    .collect(Collectors.toList());
            dto.setTasks(taskDTOs);
        }
        return dto;
    }
}
