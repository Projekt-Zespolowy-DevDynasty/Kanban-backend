package projektzespolowy.service;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import projektzespolowy.models.Task;
import projektzespolowy.models.Useer;
import projektzespolowy.repository.TaskRepository;
import projektzespolowy.repository.UserRepository;

import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    @Autowired
    public UserService(UserRepository userRepository, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
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
}
