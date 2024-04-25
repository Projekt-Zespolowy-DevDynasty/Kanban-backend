package projektzespolowy.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import projektzespolowy.DTO.CardDTO;
import projektzespolowy.DTO.TaskDTO;
import projektzespolowy.models.Card;
import projektzespolowy.models.Task;
import projektzespolowy.repository.CardRepository;
import projektzespolowy.repository.TaskRepository;
import projektzespolowy.wyjatki.ResourceNotFoundException;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TasksService {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private CardRepository cardRepository;

    public List<TaskDTO> getAllTasks() {
        List<Task> tasks = taskRepository.findAll();
        return tasks.stream()
                .map(TaskDTO::from)
                .collect(Collectors.toList());
    }

    public TaskDTO renameTask(Long taskId, String newName) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono zadania o podanym ID: " + taskId));

        if (newName.trim().isEmpty()) {
            throw new IllegalArgumentException("Nazwa zadania nie może być pusta ani składać się wyłącznie z białych znaków.");
        }

        task.setName(newName);
        taskRepository.save(task);

        return TaskDTO.from(task);
    }

    public CardDTO addTask(String taskName, Long id) {
        if (taskName.trim().isEmpty()) {
            throw new IllegalArgumentException("Nazwa zadania nie może być pusta ani składać się wyłącznie z białych znaków.");
        }
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono karty o podanym ID: " + id));

        Task newTask = new Task();
        newTask.setName(taskName);
        newTask.setColor("#8B8B8B");
        taskRepository.save(newTask);

        List<Task> tasks = card.getTasks();
        tasks.add(newTask);
        card.setTasks(tasks);
        tasks = card.getTasks();
        for (Task task : tasks) {
            task.setPosition(tasks.indexOf(task));
            taskRepository.save(task);
        }

        cardRepository.save(card);

        return CardDTO.from(card);
    }

    public TaskDTO changeTaskColor(Long taskId, String color) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono zadania o podanym ID: " + taskId));
        task.setColor(color);

        taskRepository.save(task);

        return TaskDTO.from(task);
    }
}
