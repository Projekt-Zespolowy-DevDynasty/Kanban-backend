package projektzespolowy.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import projektzespolowy.DTO.SubTasksDTO;
import projektzespolowy.models.SubTasks;
import projektzespolowy.models.Task;
import projektzespolowy.repository.SubTasksRepository;
import projektzespolowy.repository.TaskRepository;
import projektzespolowy.wyjatki.ResourceNotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
public class SubTasksService {

    private final SubTasksRepository subTasksRepository;
    private final TaskRepository taskRepository;

    @Autowired
    public SubTasksService(SubTasksRepository subTasksRepository, TaskRepository taskRepository) {
        this.subTasksRepository = subTasksRepository;
        this.taskRepository = taskRepository;
    }

    public SubTasksDTO createSubTask(Long taskId, String name) {
        Optional<Task> taskOptional = taskRepository.findById(taskId);
        SubTasksDTO subTaskDTO = new SubTasksDTO();
        subTaskDTO.setName(name);
        subTaskDTO.setFinished(false);
        if (taskOptional.isPresent()) {
            Task task = taskOptional.get();
            List<SubTasks> subTasks = task.getSubTasks();

            if (subTasks != null) { // Sprawdzenie czy lista subzadań nie jest nullem
                int maxPosition = subTasks.stream()
                        .mapToInt(SubTasks::getPosition)
                        .max()
                        .orElse(-1);

                int newPosition = maxPosition + 1;

                subTaskDTO.setPosition(newPosition);

                SubTasks subTask = toSubTask(subTaskDTO);
                subTask.setTask(task);
                SubTasks savedSubTask = subTasksRepository.save(subTask);
                return SubTasksDTO.from(savedSubTask);
            } else {
                throw new IllegalArgumentException("Lista subzadań dla zadania o identyfikatorze " + taskId + " jest niezainicjowana.");
            }
        } else {
            throw new IllegalArgumentException("Nie można znaleźć zadania o identyfikatorze: " + taskId);
        }
    }

    public int countFinishedSubTasks(Long taskId) {
        Optional<Task> taskOptional = taskRepository.findById(taskId);
        if (taskOptional.isPresent()) {
            Task task = taskOptional.get();
            List<SubTasks> subTasks = task.getSubTasks();

            if (subTasks != null) { // Dodaj warunek sprawdzający, czy lista subtasków nie jest null
                List<SubTasksDTO> subTasksDTOs = subTasks.stream()
                        .map(SubTasksDTO::from)
                        .collect(Collectors.toList());

                long finishedCount = subTasksDTOs.stream()
                        .filter(SubTasksDTO::isFinished)
                        .count();

                return (int) finishedCount;
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    public SubTasksDTO deleteSubTask(Long subTaskId) {
        Optional<SubTasks> subTaskOptional = subTasksRepository.findById(subTaskId);
        if (subTaskOptional.isPresent()) {
            SubTasks subTask = subTaskOptional.get();
            Task task = subTask.getTask();
            List<SubTasks> subTasks = task.getSubTasks();

            // Usunięcie subtaska
            subTasks.remove(subTask);
            subTasksRepository.delete(subTask);

            // Zaktualizowanie pozycji dla pozostałych subtasków
            IntStream.range(0, subTasks.size())
                    .forEach(i -> subTasks.get(i).setPosition(i));

            // Zapisz zmienione subtaski
            subTasksRepository.saveAll(subTasks);

            return SubTasksDTO.from(subTask);
        } else {
            throw new IllegalArgumentException("Subtask o identyfikatorze " + subTaskId + " nie istnieje.");
        }
    }



    public SubTasksDTO updateSubTaskFinished(Long subTaskId, boolean finished) {
        Optional<SubTasks> subTaskOptional = subTasksRepository.findById(subTaskId);
        if (subTaskOptional.isPresent()) {
            SubTasks subTask = subTaskOptional.get();
            subTask.setFinished(finished);

            SubTasks updatedSubTask = subTasksRepository.save(subTask);

            return SubTasksDTO.from(updatedSubTask);
        } else {

            return null;
        }
    }

    public SubTasksDTO updateSubTaskName(Long subTaskId, String Name) {
        Optional<SubTasks> subTaskOptional = subTasksRepository.findById(subTaskId);
        if (subTaskOptional.isPresent()) {
            SubTasks subTask = subTaskOptional.get();
            subTask.setName(Name);
            subTasksRepository.save(subTask);


            return SubTasksDTO.from(subTask);
        } else {

            return null;
        }
    }
    public int countAllSubTasks(Long taskId) {
        Optional<Task> taskOptional = taskRepository.findById(taskId);
        if (taskOptional.isPresent()) {
            Task task = taskOptional.get();
            List<SubTasks> subTasks = task.getSubTasks();
            return subTasks.size();
        } else {

            return 0;
        }
    }

    public int calculatePercentageOfFinishedSubTasks(Long taskId) {
        int finishedSubTasksCount = countFinishedSubTasks(taskId);
        int allSubTasksCount = countAllSubTasks(taskId);

        if (allSubTasksCount == 0) {
            return 0;
        }

        double percentage = ((double) finishedSubTasksCount / allSubTasksCount) * 100;
        return (int) Math.round(percentage);
    }



    private SubTasks toSubTask(SubTasksDTO subTaskDTO) {
        SubTasks subTask = new SubTasks();
        subTask.setId(subTaskDTO.getId());
        subTask.setName(subTaskDTO.getName());
        subTask.setFinished(subTaskDTO.isFinished());
        subTask.setPosition(subTaskDTO.getPosition());
        subTask.setColor(subTaskDTO.getColor());
        return subTask;
    }

    private List<SubTasksDTO> toSubTaskDTOList(List<SubTasks> subTasks) {
        return subTasks.stream()
                .map(SubTasksDTO::from)
                .collect(Collectors.toList());
    }
    public List<SubTasksDTO> getSubTasksByTaskId(Long taskId) {
        Optional<Task> taskOptional = taskRepository.findById(taskId);
        if (taskOptional.isPresent()) {
            Task task = taskOptional.get();
            List<SubTasks> subTasks = task.getSubTasks();
            return toSubTaskDTOList(subTasks);
        } else {
            return List.of();
        }
    }
}
