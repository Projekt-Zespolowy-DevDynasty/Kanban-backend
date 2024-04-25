package projektzespolowy.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import projektzespolowy.DTO.SubTasksDTO;
import projektzespolowy.models.SubTasks;
import projektzespolowy.models.Task;
import projektzespolowy.repository.SubTasksRepository;
import projektzespolowy.repository.TaskRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubTasksServiceTest {

    @Mock
    private SubTasksRepository subTasksRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private SubTasksService subTasksService;

    private SubTasks subTask;
    private Task task;

    @BeforeEach
    void setUp() {
        subTask = new SubTasks();
        subTask.setId(1L);
        subTask.setName("SubTask 1");
        subTask.setFinished(false);
        subTask.setPosition(1);
        subTask.setColor("Red");

        task = new Task();
        task.setId(1L);
        task.setSubTasks(List.of(subTask));
    }

    @Test
    void testCreateSubTask() {

        Long taskId = 1L;
        String subTaskName = "Test SubTask";

        Task task = new Task();
        task.setId(taskId);
        List<SubTasks> subTasks = new ArrayList<>();
        task.setSubTasks(subTasks);

        SubTasksDTO subTaskDTO = new SubTasksDTO();
        subTaskDTO.setName(subTaskName);
        subTaskDTO.setFinished(false);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));

        ArgumentCaptor<SubTasks> subTaskCaptor = ArgumentCaptor.forClass(SubTasks.class);
        when(subTasksRepository.save(subTaskCaptor.capture())).thenAnswer(invocation -> {
            SubTasks savedSubTask = invocation.getArgument(0);
            savedSubTask.setId(1L);
            return savedSubTask;
        });


        SubTasksDTO createdSubTask = subTasksService.createSubTask(taskId, subTaskName);


        assertTrue(subTaskCaptor.getValue().getTask() == task);
        assertNotNull(createdSubTask);
        assertEquals(subTaskName, createdSubTask.getName());
        assertFalse(createdSubTask.isFinished());
        assertEquals(0, createdSubTask.getPosition());
    }
    @Test
    void testCountFinishedSubTasks() {

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));


        int finishedSubTasksCount = subTasksService.countFinishedSubTasks(1L);


        assertEquals(0, finishedSubTasksCount);
    }

    @Test
    void testUpdateSubTaskFinished() {

        Long subTaskId = 1L;
        boolean finished = true;

        SubTasks subTask = new SubTasks();
        subTask.setId(subTaskId);
        subTask.setFinished(!finished);

        when(subTasksRepository.findById(subTaskId)).thenReturn(Optional.of(subTask));

        ArgumentCaptor<SubTasks> subTaskCaptor = ArgumentCaptor.forClass(SubTasks.class);
        when(subTasksRepository.save(subTaskCaptor.capture())).thenAnswer(invocation -> {
            SubTasks updatedSubTask = invocation.getArgument(0);

            return updatedSubTask;
        });


        SubTasksDTO updatedSubTaskDTO = subTasksService.updateSubTaskFinished(subTaskId, finished);


        assertNotNull(updatedSubTaskDTO);
        assertTrue(subTaskCaptor.getValue().isFinished() == finished);
        assertEquals(subTask.getId(), updatedSubTaskDTO.getId());
        assertEquals(subTask.getName(), updatedSubTaskDTO.getName());
        assertEquals(subTask.getPosition(), updatedSubTaskDTO.getPosition());
    }

    @Test
    void testUpdateSubTaskName() {

        when(subTasksRepository.findById(1L)).thenReturn(Optional.of(subTask));


        SubTasksDTO updatedSubTask = subTasksService.updateSubTaskName(1L, "Updated Name");


        assertNotNull(updatedSubTask);
        assertEquals("Updated Name", updatedSubTask.getName());
    }

    @Test
    void testCountAllSubTasks() {

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));


        int allSubTasksCount = subTasksService.countAllSubTasks(1L);


        assertEquals(1, allSubTasksCount);
    }

    @Test
    void testCalculatePercentageOfFinishedSubTasks() {

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));


        int percentage = subTasksService.calculatePercentageOfFinishedSubTasks(1L);


        assertEquals(0, percentage);
    }

    @Test
    void testGetSubTasksByTaskId() {

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));


        List<SubTasksDTO> subTasks = subTasksService.getSubTasksByTaskId(1L);


        assertEquals(1, subTasks.size());
    }
}
