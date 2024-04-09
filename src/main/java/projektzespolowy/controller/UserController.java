package projektzespolowy.controller;

import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projektzespolowy.models.Task;
import projektzespolowy.models.Useer;
import projektzespolowy.repository.TaskRepository;
import projektzespolowy.repository.UserRepository;
import projektzespolowy.service.UserService;
import projektzespolowy.utils.ColorGenerator;

import java.util.List;
import java.util.Optional;

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
    //Dodaje uzytkownika
    @PostMapping("/add")
    public Useer addUser(@RequestBody Useer user) {
        user.setColor(ColorGenerator.getRandomLightColor());
        user.setMaxUserTasksLimit(3);
        return userRepository.save(user);
    }
    //Zwraca wszystkich uzytkownikow
    @GetMapping("/get")
    public List<Useer> getAllUsers() {
        return userRepository.findAll();
    }
    //przypisuje uzytkownika do taska
    @PostMapping("/{userId}/assignToTask/{taskId}")
    public ResponseEntity<String> assignUserToTask(@PathVariable Long userId, @PathVariable Long taskId) {
        Optional<Useer> userOptional = userRepository.findById(userId);
        Optional<Task> taskOptional = taskRepository.findById(taskId);

        if (!userOptional.isPresent() || !taskOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Task task = taskOptional.get();
        Useer user = userOptional.get();


        if (task.getUseers().contains(user)) {
            return ResponseEntity.badRequest().body("Użytkownik już przypisany do tego zadania");
        }


        task.getUseers().add(user);
        //user.getTasks().add(task);

        taskRepository.save(task);
        userRepository.save(user);
        return ResponseEntity.ok("Przypisano uzytkownika do taska");
    }

// usuwa uzytkownika z taska
    @DeleteMapping("/{userId}/removeFromTask/{taskId}")
    public ResponseEntity<String> removeUserFromTask(@PathVariable Long userId, @PathVariable Long taskId) {
        Optional<Useer> userOptional = userRepository.findById(userId);
        Optional<Task> taskOptional = taskRepository.findById(taskId);

        if (!userOptional.isPresent() || !taskOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Task task = taskOptional.get();
        Useer user = userOptional.get();

        boolean removed = task.getUseers().remove(user);
        if (!removed) {
            return ResponseEntity.badRequest().body("uzytkownik nie dodany do taska");
        }

        //user.getTasks().remove(task);
        taskRepository.save(task);
        userRepository.save(user);

        return ResponseEntity.ok("usunieto poprawnie");
    }

// usuwa w ogole uzytkownika
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
    // Wyświetla wszystkich użytkowników przypisanych do danego zadania
    @GetMapping("/{taskId}/usersAssigned")
    public ResponseEntity<List<Useer>> getUsersAssignedToTask(@PathVariable Long taskId) {
        Optional<Task> taskOptional = taskRepository.findById(taskId);

        if (!taskOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Task task = taskOptional.get();
        List<Useer> usersAssigned = task.getUseers();

        return ResponseEntity.ok(usersAssigned);
    }

    // Wyświetla wszystkich użytkowników nieprzypisanych do danego zadania
    @GetMapping("/{taskId}/usersNotAssigned")
    public ResponseEntity<List<Useer>> getUsersNotAssignedToTask(@PathVariable Long taskId) {
        Optional<Task> taskOptional = taskRepository.findById(taskId);

        if (!taskOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Task task = taskOptional.get();
        List<Useer> allUsers = userRepository.findAll();
        List<Useer> usersAssigned = task.getUseers();

        allUsers.removeAll(usersAssigned);

        return ResponseEntity.ok(allUsers);
    }
    @PatchMapping("/{userId}/setMaxTasksLimit")
    public ResponseEntity<String> setMaxTasksLimit(@PathVariable Long userId, @RequestParam int maxTasksLimit) {
        Optional<Useer> userOptional = userRepository.findById(userId);

        if (!userOptional.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Useer user = userOptional.get();

        if (maxTasksLimit == -1) {
            user.setMaxUserTasksLimit(Integer.MAX_VALUE);
        } else {
            user.setMaxUserTasksLimit(maxTasksLimit);
        }

        userRepository.save(user);

        if (maxTasksLimit == -1) {
            return ResponseEntity.ok("Limit zadań dla użytkownika ustawiony na praktycznie nieograniczony.");
        } else {
            return ResponseEntity.ok("Nowy limit zadań dla użytkownika: " + maxTasksLimit);
        }
    }

}
