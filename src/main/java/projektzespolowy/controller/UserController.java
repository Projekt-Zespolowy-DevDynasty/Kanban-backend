package projektzespolowy.controller;

import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final UserService userService;

    @Autowired
    public UserController(UserRepository userRepository, TaskRepository taskRepository, UserService userService) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.userService = userService;
    }

    @PostMapping("/add")
    public ResponseEntity<UseerDTO> addUser(@RequestBody UseerDTO userDTO) {
        Useer user = new Useer();
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());
        user.setColor(ColorGenerator.getRandomLightColor());
        user.setMaxUserTasksLimit(3);
        Useer savedUser = userRepository.save(user);
        return ResponseEntity.ok(UseerDTO.from(savedUser));
    }

    @GetMapping("/get")
    public ResponseEntity<List<UseerDTO>> getAllUsers() {
        List<Useer> users = userRepository.findAll();
        List<UseerDTO> userDTOs = users.stream()
                .map(UseerDTO::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(userDTOs);
    }
    @GetMapping("/{id}")
    public ResponseEntity<UseerWithTasksDTO> getUserById(@PathVariable Long id) {
        Useer user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return ResponseEntity.ok(UseerWithTasksDTO.from(user));
    }

    @PostMapping("/{userId}/assignToTask/{taskId}")
    public ResponseEntity<String> assignUserToTask(@PathVariable Long userId, @PathVariable Long taskId) {
        Useer user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));

        if (user.getTasks().size() >= user.getMaxUserTasksLimit()) {
            return ResponseEntity.badRequest().body("Limit zadań użytkownika został osiągnięty");
        }


        if (task.getUseers().contains(user)) {
            return ResponseEntity.badRequest().body("Użytkownik już przypisany do tego zadania");
        }

        task.getUseers().add(user);
        taskRepository.save(task);
        return ResponseEntity.ok().body("Przypisano użytkownika do zadania");
    }

    @DeleteMapping("/{userId}/removeFromTask/{taskId}")
    public ResponseEntity<String> removeUserFromTask(@PathVariable Long userId, @PathVariable Long taskId) {
        Useer user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        boolean removed = task.getUseers().remove(user);
        if (!removed) {
            return ResponseEntity.badRequest().body("Użytkownik nie był dodany do tego zadania");
        }

        taskRepository.save(task);
        return ResponseEntity.ok().body("Usunięto użytkownika z zadania");
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().body("Użytkownik został usunięty");
    }

    @GetMapping("/{taskId}/usersAssigned")
    public ResponseEntity<List<UseerDTO>> getUsersAssignedToTask(@PathVariable Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        List<UseerDTO> usersAssignedDTOs = task.getUseers().stream()
                .map(UseerDTO::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(usersAssignedDTOs);

    }

    @GetMapping("/{taskId}/usersNotAssigned")
    public ResponseEntity<List<UseerDTO>> getUsersNotAssignedToTask(@PathVariable Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        List<Useer> allUsers = userRepository.findAll();
        List<Useer> usersAssigned = task.getUseers();

        List<UseerDTO> usersNotAssignedDTOs = allUsers.stream()
                .filter(user -> !usersAssigned.contains(user))
                .map(UseerDTO::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(usersNotAssignedDTOs);
}

    @PutMapping("/{userId}/setMaxTasksLimit")
    public ResponseEntity<String> setMaxTasksLimit(@PathVariable Long userId, @RequestBody int maxTasksLimit) {
        Useer user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (maxTasksLimit == -1) {
            user.setMaxUserTasksLimit(Integer.MAX_VALUE);
        } else {
            user.setMaxUserTasksLimit(maxTasksLimit);
        }
        userRepository.save(user);
        return ResponseEntity.ok(maxTasksLimit == -1 ? "Limit zadań dla użytkownika ustawiony na praktycznie nieograniczony." : "Nowy limit zadań dla użytkownika: " + maxTasksLimit);
    }


}

