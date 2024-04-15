package projektzespolowy.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import projektzespolowy.models.Task;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskWithoutUserDTO {
    private Long id;
    private String name;
    private String color;
    private int position;

    public static TaskWithoutUserDTO from(Task task) {
        TaskWithoutUserDTO dto = new TaskWithoutUserDTO();
        dto.setId(task.getId());
        dto.setName(task.getName());
        dto.setColor(task.getColor());
        dto.setPosition(task.getPosition());
        return dto;
    }
}
