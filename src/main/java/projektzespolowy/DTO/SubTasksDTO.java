package projektzespolowy.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import projektzespolowy.models.SubTasks;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SubTasksDTO {
    private Long id;
    private String name;
    private boolean finished;
    private int position;
    private String color;
    private Long taskId;

    public static SubTasksDTO from(SubTasks subTask) {
        SubTasksDTO dto = new SubTasksDTO();
        dto.setId(subTask.getId());
        dto.setName(subTask.getName());
        dto.setFinished(subTask.isFinished());
        dto.setPosition(subTask.getPosition());
        dto.setColor(subTask.getColor());


        if (subTask.getTask() != null) {
            dto.setTaskId(subTask.getTask().getId());
        }

        return dto;
    }
    public static SubTasks to(SubTasksDTO dto) {
        SubTasks subTask = new SubTasks();
        subTask.setId(dto.getId());
        subTask.setName(dto.getName());
        subTask.setFinished(dto.isFinished());
        subTask.setPosition(dto.getPosition());
        subTask.setColor(dto.getColor());

        return subTask;
    }


}
