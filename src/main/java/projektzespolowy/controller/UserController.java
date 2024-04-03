package projektzespolowy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projektzespolowy.models.Task;
import projektzespolowy.models.Useer;
import projektzespolowy.repository.TaskRepository;
import projektzespolowy.repository.UserRepository;
import projektzespolowy.utils.ColorGenerator;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    @Autowired
    public UserController(UserRepository userRepository, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }


    @PostMapping("/add")
    public Useer addUser(@RequestBody Useer user) {
        user.setColor(ColorGenerator.getRandomLightColor());
        return userRepository.save(user);
    }


    @GetMapping("/get")
    public List<Useer> getAllUsers() {
        return userRepository.findAll();
    }
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
        user.getTasks().add(task);

        taskRepository.save(task);
        userRepository.save(user);

        return ResponseEntity.ok("Przypisano użytkownika do zadania");
    }


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

        user.getTasks().remove(task);
        taskRepository.save(task);
        userRepository.save(user);

        return ResponseEntity.ok("usunieto poprawnie");
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {
        if (!userRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        userRepository.deleteById(id);
        return ResponseEntity.ok("Uzytkownik usuniety calkowicie");
    }
}
