package projektzespolowy.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import projektzespolowy.DTO.CardDTO;
import projektzespolowy.DTO.TaskDTO;
import projektzespolowy.models.Card;
import projektzespolowy.models.Task;
import projektzespolowy.repository.CardRepository;
import projektzespolowy.repository.TaskRepository;
import projektzespolowy.service.TasksService;
import projektzespolowy.wyjatki.ResourceNotFoundException;

public class TasksServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private TasksService tasksService;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetAllTasks() {

        List<Task> tasks = new ArrayList<>();
        Task task1 = new Task();
        task1.setId(1L);
        task1.setName("Task 1");
        tasks.add(task1);
        Task task2 = new Task();
        task2.setId(2L);
        task2.setName("Task 2");
        tasks.add(task2);
        when(taskRepository.findAll()).thenReturn(tasks);


        List<TaskDTO> taskDTOs = tasksService.getAllTasks();


        assertEquals(2, taskDTOs.size());
        assertEquals("Task 1", taskDTOs.get(0).getName());
        assertEquals("Task 2", taskDTOs.get(1).getName());
    }

    @Test
    public void testRenameTask() {

        Task task = new Task();
        task.setId(1L);
        task.setName("Old Name");
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));


        TaskDTO renamedTaskDTO = tasksService.renameTask(1L, "New Name");


        assertEquals("New Name", renamedTaskDTO.getName());
    }

    @Test
    public void testAddTask() {

        Card card = new Card();
        card.setId(1L);
        List<Task> tasks = new ArrayList<>();
        card.setTasks(tasks);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));


        CardDTO cardDTO = tasksService.addTask("New Task", 1L);


        assertEquals(1, cardDTO.getTasks().size());
        assertEquals("New Task", cardDTO.getTasks().get(0).getName());
    }

    @Test
    public void testChangeTaskColor() {

        Task task = new Task();
        task.setId(1L);
        task.setColor("#8B8B8B");
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));


        TaskDTO changedTaskDTO = tasksService.changeTaskColor(1L, "#FFFFFF");


        assertEquals("#FFFFFF", changedTaskDTO.getColor());
    }

    @Test
    public void testAddTaskWithEmptyName() {

        Card card = new Card();
        card.setId(1L);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(card));


        assertThrows(IllegalArgumentException.class, () -> tasksService.addTask("", 1L));
    }

    @Test
    public void testRenameTaskWithEmptyName() {

        Long taskId = 1L;


        assertThrows(IllegalArgumentException.class, () -> tasksService.renameTask(taskId, ""));
    }


    @Test
    public void testChangeTaskColorWithInvalidId() {

        assertThrows(IllegalArgumentException.class, () -> tasksService.changeTaskColor(999L, "#FFFFFF"));
    }

    @Test
    public void testGetAllTasksWithNoTasks() {

        when(taskRepository.findAll()).thenReturn(new ArrayList<>());


        List<TaskDTO> taskDTOs = tasksService.getAllTasks();


        assertEquals(0, taskDTOs.size());
    }

    @Test
    public void testRenameNonExistentTask() {

        when(taskRepository.findById(999L)).thenReturn(Optional.empty());


        assertThrows(ResourceNotFoundException.class, () -> tasksService.renameTask(999L, "New Name"));
    }

    @Test
    public void testChangeColorOfNonExistentTask() {

        when(taskRepository.findById(999L)).thenReturn(Optional.empty());


        assertThrows(IllegalArgumentException.class, () -> tasksService.changeTaskColor(999L, "#FFFFFF"));
    }
}
