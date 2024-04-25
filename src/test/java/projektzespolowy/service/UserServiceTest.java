package projektzespolowy.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import projektzespolowy.models.Task;
import projektzespolowy.models.Useer;
import projektzespolowy.repository.TaskRepository;
import projektzespolowy.repository.UserRepository;
import projektzespolowy.utils.ColorGenerator;
import projektzespolowy.wyjatki.ResourceNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private UserService userService;

    private Useer user;
    private Task task;

    @BeforeEach
    void setUp() {
        user = new Useer();
        user.setId(1L);
        user.setFirstName("John");
        user.setLastName("Doe");
        user.setEmail("john.doe@example.com");
        user.setColor(ColorGenerator.getRandomLightColor());
        user.setMaxUserTasksLimit(3);

        task = new Task();
        task.setId(1L);
        task.setName("Task 1");
        task.setUseers(new ArrayList<>());
    }

    @Test
    void testAddUser() {

        when(userRepository.save(Mockito.any(Useer.class))).thenReturn(user);


        Useer addedUser = userService.addUser(user);


        Assertions.assertNotNull(addedUser);
        Assertions.assertEquals("John", addedUser.getFirstName());
        Assertions.assertEquals("Doe", addedUser.getLastName());
        Assertions.assertEquals("john.doe@example.com", addedUser.getEmail());
        Assertions.assertNotNull(addedUser.getColor());
        Assertions.assertEquals(3, addedUser.getMaxUserTasksLimit());
    }

    @Test
    void testGetAllUsers() {

        List<Useer> users = List.of(user);
        when(userRepository.findAll()).thenReturn(users);


        List<Useer> retrievedUsers = userService.getAllUsers();


        Assertions.assertEquals(1, retrievedUsers.size());
        Assertions.assertEquals("John", retrievedUsers.get(0).getFirstName());
        Assertions.assertEquals("Doe", retrievedUsers.get(0).getLastName());
        Assertions.assertEquals("john.doe@example.com", retrievedUsers.get(0).getEmail());
    }

    @Test
    void testGetUserById() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));


        Useer retrievedUser = userService.getUserById(1L);


        Assertions.assertNotNull(retrievedUser);
        Assertions.assertEquals("John", retrievedUser.getFirstName());
        Assertions.assertEquals("Doe", retrievedUser.getLastName());
        Assertions.assertEquals("john.doe@example.com", retrievedUser.getEmail());
    }

    @Test
    void testAssignUserToTask() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));


        String result = userService.assignUserToTask(1L, 1L);


        Assertions.assertEquals("Przypisano użytkownika do zadania", result);
        Assertions.assertTrue(task.getUseers().contains(user));
    }

    @Test
    void testRemoveUserFromTask() {

        task.getUseers().add(user);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));


        String result = userService.removeUserFromTask(1L, 1L);


        Assertions.assertEquals("Usunięto użytkownika z zadania", result);
        Assertions.assertFalse(task.getUseers().contains(user));
    }

    @Test
    void testDeleteUser() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(taskRepository.findAllWithUser(1L)).thenReturn(new ArrayList<>());


        userService.deleteUser(1L);


        verify(userRepository).deleteById(1L);
    }

    @Test
    void testGetUsersAssignedToTask() {

        task.getUseers().add(user);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));


        List<Useer> users = userService.getUsersAssignedToTask(1L);


        Assertions.assertEquals(1, users.size());
        Assertions.assertTrue(users.contains(user));
    }

    @Test
    void testGetUsersNotAssignedToTask() {

        Useer otherUser = new Useer();
        otherUser.setId(2L);
        otherUser.setFirstName("Jane");
        otherUser.setLastName("Smith");
        otherUser.setEmail("jane.smith@example.com");
        otherUser.setColor(ColorGenerator.getRandomLightColor());
        otherUser.setMaxUserTasksLimit(3);
        List<Useer> allUsers = List.of(user, otherUser);
        when(userRepository.findAll()).thenReturn(allUsers);
        task.getUseers().add(user);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));


        List<Useer> users = userService.getUsersNotAssignedToTask(1L);


        Assertions.assertEquals(1, users.size());
        Assertions.assertFalse(users.contains(user));
        Assertions.assertTrue(users.contains(otherUser));
    }
}
