package projektzespolowy.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import projektzespolowy.models.Useer;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UseerDTO {
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String color;
    private int maxUserTasksLimit;

    public static UseerDTO from(Useer user) {
        UseerDTO dto = new UseerDTO();
        dto.setId(user.getId());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setEmail(user.getEmail());
        dto.setMaxUserTasksLimit(user.getMaxUserTasksLimit());
        return dto;
    }
}
