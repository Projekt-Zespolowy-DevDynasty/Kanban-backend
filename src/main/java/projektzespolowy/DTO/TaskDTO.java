package projektzespolowy.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import projektzespolowy.models.Task;
import projektzespolowy.models.Useer;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TaskDTO {
    private Long id;
    private String name;
    private String color;
    private int position;
    private List<UseerDTO> useers;
    private List<SubTasksDTO> subTasks;

    public static TaskDTO from(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setName(task.getName());
        dto.setColor(task.getColor());
        dto.setPosition(task.getPosition());

        if (task.getUseers() != null) {
            List<UseerDTO> userDTOs = task.getUseers().stream()
                    .map(UseerDTO::from)
                    .collect(Collectors.toList());
            dto.setUseers(userDTOs);
        }

        if (task.getSubTasks() != null) {
            List<SubTasksDTO> subTasksDTOs = task.getSubTasks().stream()
                    .map(SubTasksDTO::from)
                    .collect(Collectors.toList());
            dto.setSubTasks(subTasksDTOs);
        }

        return dto;
    }
}

