package projektzespolowy.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import projektzespolowy.models.Useer;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UseerWithTasksDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String color;
    private int maxUserTasksLimit;
    private List<TaskWithoutUserDTO> tasks;

    public static UseerWithTasksDTO from(Useer user) {
        UseerWithTasksDTO dto = new UseerWithTasksDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setColor(user.getColor());
        dto.setMaxUserTasksLimit(user.getMaxUserTasksLimit());
        dto.setTasks(user.getTasks().stream()
                .map(TaskWithoutUserDTO::from)
                .collect(Collectors.toList()));
        return dto;
    }
}
