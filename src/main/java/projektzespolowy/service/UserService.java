package projektzespolowy.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import projektzespolowy.DTO.UseerDTO;
import projektzespolowy.DTO.UseerWithTasksDTO;
import projektzespolowy.models.Task;
import projektzespolowy.models.Useer;
import projektzespolowy.repository.TaskRepository;
import projektzespolowy.repository.UserRepository;
import projektzespolowy.utils.ColorGenerator;
import projektzespolowy.wyjatki.ResourceNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    @Autowired
    public UserService(UserRepository userRepository, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    public Useer addUser(Useer user) {
        Useer userVar = new Useer();
        userVar.setFirstName(user.getFirstName());
        userVar.setLastName(user.getLastName());
        userVar.setEmail(user.getEmail());
        userVar.setColor(ColorGenerator.getRandomLightColor());
        userVar.setMaxUserTasksLimit(3);
        userRepository.save(userVar);
        return userVar;
    }
    public List<Useer> getAllUsers() {
        List<Useer> users = userRepository.findAll();
        return users;
    }
    public Useer getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
    public String assignUserToTask(Long userId, Long taskId) {
        Useer user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found"));


        if (user.getTasks() == null) {

            user.setTasks(new ArrayList<>());
        }


        if (user.getTasks().size() >= user.getMaxUserTasksLimit()) {
            throw new IllegalArgumentException("Nazwa karty nie może być pusta ani składać się wyłącznie z białych znaków.");
        }

        if (task.getUseers().contains(user)) {
            throw new IllegalArgumentException("Użytkownik jest już przypisany do tego zadania");
        }

        task.getUseers().add(user);
        taskRepository.save(task);
        return "Przypisano użytkownika do zadania";
    }


    public String removeUserFromTask(Long userId, Long taskId) {
        Useer user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        boolean removed = task.getUseers().remove(user);
        if (!removed) {
            throw new IllegalArgumentException("Użytkownik nie był dodany do tego zadania");
        }

        taskRepository.save(task);
        return "Usunięto użytkownika z zadania";
    }
    @Transactional
    public void deleteUser(Long id) {
        // Sprawdź, czy użytkownik istnieje
        Useer user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono użytkownika o identyfikatorze: " + id));

        // Znajdź wszystkie zadania, do których przypisany jest użytkownik
        List<Task> tasks = taskRepository.findAllWithUser(id); // Metoda do zaimplementowania

        // Usuń użytkownika z każdego zadania
        for (Task task : tasks) {
            task.getUseers().remove(user);
            taskRepository.save(task); // Zapisz zmiany w zadaniach
        }

        // Usuń użytkownika
        userRepository.deleteById(id);
    }
    public List<Useer> getUsersAssignedToTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        return task.getUseers();
    }

    public List<Useer> getUsersNotAssignedToTask(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Task not found"));
        List<Useer> allUsers = userRepository.findAll();
        List<Useer> usersAssigned = task.getUseers();

        List<Useer> usersNotAssigned = allUsers.stream()
                .filter(user -> !usersAssigned.contains(user))
                .toList();
        return usersNotAssigned;
    }
}
