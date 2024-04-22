package projektzespolowy.controller;

import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projektzespolowy.DTO.UseerDTO;
import projektzespolowy.DTO.UseerWithTasksDTO;
import projektzespolowy.models.Task;
import projektzespolowy.models.Useer;
import projektzespolowy.repository.TaskRepository;
import projektzespolowy.repository.UserRepository;
import projektzespolowy.service.UserService;
import projektzespolowy.utils.ColorGenerator;
import projektzespolowy.wyjatki.ResourceNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/users")
public class UserController {
// http://localhost:8080/swagger-ui/index.html#
    private final UserRepository userRepository;
    private final UserService userService;

    @Autowired
    public UserController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }

    @PostMapping("/add")
    public ResponseEntity<UseerDTO> addUser(@RequestBody Useer user) {
        Useer userVar = userService.addUser(user);
        UseerDTO userToDto = UseerDTO.from(userVar);
        return ResponseEntity.ok(userToDto);
    }

    @GetMapping("/get")
    public ResponseEntity<List<UseerDTO>> getAllUsers() {
        List<Useer> users = userService.getAllUsers();
        List<UseerDTO> userDTOs = users.stream()
                .map(UseerDTO::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDTOs);
    }
    @GetMapping("/{id}")
    public ResponseEntity<UseerWithTasksDTO> getUserById(@PathVariable Long id) {
        Useer user = userService.getUserById(id);
        return ResponseEntity.ok(UseerWithTasksDTO.from(user));
    }

    @PostMapping("/{userId}/assignToTask/{taskId}")
    public ResponseEntity<String> assignUserToTask(@PathVariable Long userId, @PathVariable Long taskId) {
        String message = userService.assignUserToTask(userId, taskId);
        return ResponseEntity.ok().body(message);
    }

    @DeleteMapping("/{userId}/removeFromTask/{taskId}")
    public ResponseEntity<String> removeUserFromTask(@PathVariable Long userId, @PathVariable Long taskId) {
        String message = userService.removeUserFromTask(userId, taskId);
        return ResponseEntity.ok().body(message);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().body("Użytkownik został usunięty");
    }

    @GetMapping("/{taskId}/usersAssigned")
    public ResponseEntity<List<UseerDTO>> getUsersAssignedToTask(@PathVariable Long taskId) {
        List<Useer> usersInTask = userService.getUsersAssignedToTask(taskId);

        List<UseerDTO> usersAssignedDTOs = usersInTask.stream()
                .map(UseerDTO::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(usersAssignedDTOs);

    }

    @GetMapping("/{taskId}/usersNotAssigned")
    public ResponseEntity<List<UseerDTO>> getUsersNotAssignedToTask(@PathVariable Long taskId) {

        List<Useer> usersNotAssigned = userService.getUsersNotAssignedToTask(taskId);
        List<UseerDTO> usersNotAssignedDTOs = usersNotAssigned.stream()
                .map(UseerDTO::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(usersNotAssignedDTOs);
    }

    @PutMapping("/{userId}/setMaxTasksLimit")
    public ResponseEntity<String> setMaxTasksLimit(@PathVariable Long userId, @RequestBody int maxTasksLimit) {
        Useer user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if(user.getTasks().size() > maxTasksLimit) {
            return ResponseEntity.status(HttpStatusCode.valueOf(415)).body("Użytkownik ma więcej zadań niż nowy limit");
        }

        if(maxTasksLimit < 0) {
            return ResponseEntity.status(HttpStatusCode.valueOf(414)).body("Limit zadań nie może być ujemny");
        }

        user.setMaxUserTasksLimit(maxTasksLimit);
        userRepository.save(user);
        return ResponseEntity.ok("Nowy limit zadań dla użytkownika: " + maxTasksLimit);
    }


}

