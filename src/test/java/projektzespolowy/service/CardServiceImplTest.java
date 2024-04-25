package projektzespolowy.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import projektzespolowy.repository.CardRepository;
import projektzespolowy.repository.TaskRepository;
import projektzespolowy.repository.RowRepository;
import projektzespolowy.models.Card;
import projektzespolowy.models.Task;
import projektzespolowy.models.RowWithAllCards;
import projektzespolowy.DTO.CardDTO;
import projektzespolowy.wyjatki.ResourceNotFoundException;
import java.util.Optional;
import java.util.ArrayList;
import java.util.List;

public class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private RowRepository rowWithAllCardsRepository;

    @InjectMocks
    private CardServiceImpl cardService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetCard() {

        Card mockCard = new Card();
        mockCard.setId(1L);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(mockCard));


        Card result = cardService.getCard(1L);


        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testGetAllCards() {

        Card card1 = new Card("Card 1", 5, 1);
        Card card2 = new Card("Card 2", 3, 2);
        Card cardToDo = new Card("To do", Integer.MAX_VALUE, 0);
        Card cardDone = new Card("Done", Integer.MAX_VALUE, 3);

        List<Card> allCards = new ArrayList<>();
        allCards.add(card1);
        allCards.add(card2);
        allCards.add(cardToDo);
        allCards.add(cardDone);


        when(cardRepository.findAll()).thenReturn(allCards);
        when(cardRepository.findByName("To do")).thenReturn(Optional.of(cardToDo));
        when(cardRepository.findByName("Done")).thenReturn(Optional.of(cardDone));


        List<Card> result = cardService.getAllCards();


        assertNotNull(result);
        assertEquals(4, result.size());
        assertEquals("To do", result.get(0).getName());
        assertEquals("Card 1", result.get(1).getName());
        assertEquals("Card 2", result.get(2).getName());
        assertEquals("Done", result.get(3).getName());
        assertEquals(Integer.MAX_VALUE, result.get(0).getMaxTasksLimit());
        assertEquals(Integer.MAX_VALUE, result.get(3).getMaxTasksLimit());
        assertEquals(0, result.get(0).getPosition());
        assertEquals(3, result.get(3).getPosition());
    }

    @Test
    void testAddCardWithValidName() {

        CardDTO cardDTO = new CardDTO();
        cardDTO.setName("New Card");
        cardDTO.setMaxTasksLimit(5);


        Card doneCard = new Card();
        doneCard.setName("Done");
        doneCard.setPosition(1);
        when(cardRepository.findByName("Done")).thenReturn(Optional.of(doneCard));


        Card result = cardService.addCard(cardDTO);


        assertNotNull(result);
        assertEquals("New Card", result.getName());
        assertEquals(5, result.getMaxTasksLimit());
        assertEquals(1, result.getPosition());
    }

    @Test
    void testAddCardWithEmptyName() {

        CardDTO cardDTO = new CardDTO();
        cardDTO.setName("");


        assertThrows(IllegalArgumentException.class, () -> cardService.addCard(cardDTO));
    }

    @Test
    void testAddCardWithWhitespaceName() {

        CardDTO cardDTO = new CardDTO();
        cardDTO.setName("   ");


        assertThrows(IllegalArgumentException.class, () -> cardService.addCard(cardDTO));
    }

    @Test
    void testAddCardWithNameToDo() {

        CardDTO cardDTO = new CardDTO();
        cardDTO.setName("To do");


        assertThrows(UnsupportedOperationException.class, () -> cardService.addCard(cardDTO));
    }

    @Test
    void testAddCardWithNameDone() {

        CardDTO cardDTO = new CardDTO();
        cardDTO.setName("Done");


        assertThrows(UnsupportedOperationException.class, () -> cardService.addCard(cardDTO));
    }

    @Test
    void testDeleteTaskFromCard() {

        Long cardId = 1L;
        Long taskId = 100L;

        Card card = new Card();
        card.setId(cardId);
        card.setTasks(new ArrayList<>());
        Task task = new Task();
        task.setId(taskId);
        card.getTasks().add(task);

        when(cardRepository.findById(cardId)).thenReturn(Optional.of(card));
        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));


        cardService.deleteTaskFromCard(cardId, taskId);


        assertFalse(card.getTasks().contains(task));


        verify(cardRepository, times(1)).save(card);
    }

    @Test
    void testDeleteTaskFromCardNotFound() {

        Long cardId = 1L;
        Long taskId = 100L;

        when(cardRepository.findById(cardId)).thenReturn(Optional.empty());


        assertThrows(ResourceNotFoundException.class, () -> cardService.deleteTaskFromCard(cardId, taskId));


        verify(cardRepository, never()).save(any());
    }

    @Test
    void testUpdateMaxTasksLimit() {
        // Arrange
        Card mockCard = new Card();
        mockCard.setId(1L);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(mockCard));
        when(rowWithAllCardsRepository.findAll()).thenReturn(new ArrayList<>());


        assertDoesNotThrow(() -> cardService.updateMaxTasksLimit(1L, 10));


        assertEquals(10, mockCard.getMaxTasksLimit());
    }

    @Test
    void testEditColumnName() {
        // Arrange
        Card mockCard = new Card();
        mockCard.setId(1L);
        when(cardRepository.findById(1L)).thenReturn(Optional.of(mockCard));

        // Act
        assertDoesNotThrow(() -> cardService.editColumnName(1L, "New Name"));

        // Assert
        assertEquals("New Name", mockCard.getName());
    }

    @Test
    public void testUpdateCardPosition() {

        Card destinationCard = new Card();
        destinationCard.setId(1L);
        destinationCard.setPosition(1);

        Card sourceCard = new Card();
        sourceCard.setId(2L);
        sourceCard.setPosition(2);


        List<Card> cardsBetween = new ArrayList<>();
        when(cardRepository.findById(1L)).thenReturn(Optional.of(destinationCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(sourceCard));
        when(cardRepository.findAllByPositionGreaterThanAndPositionLessThan(1, 2)).thenReturn(cardsBetween);


        assertDoesNotThrow(() -> cardService.updateCardPosition(1L, 2L));


        assertEquals(2, destinationCard.getPosition());
        assertEquals(1, sourceCard.getPosition());
    }

    @Test
    public void testMoveTask() {

        Card sourceCard = new Card();
        sourceCard.setId(1L);
        List<Task> sourceTasks = new ArrayList<>();
        Task taskToMove = new Task();
        taskToMove.setId(1L);
        taskToMove.setPosition(0);
        sourceTasks.add(taskToMove);
        sourceCard.setTasks(sourceTasks);

        Card destinationCard = new Card();
        destinationCard.setId(2L);
        List<Task> destinationTasks = new ArrayList<>();
        destinationCard.setTasks(destinationTasks);

        when(cardRepository.findById(1L)).thenReturn(Optional.of(sourceCard));
        when(cardRepository.findById(2L)).thenReturn(Optional.of(destinationCard));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(taskToMove));


        cardService.moveTask(1L, 1L, 2L, 0);


        assertEquals(0, sourceCard.getTasks().size());
        assertEquals(1, destinationCard.getTasks().size());
        assertEquals(taskToMove, destinationCard.getTasks().get(0));
        assertEquals(0, destinationCard.getTasks().get(0).getPosition());
        verify(cardRepository, times(1)).save(sourceCard);
        verify(cardRepository, times(1)).save(destinationCard);
        verify(taskRepository, times(1)).saveAll(destinationCard.getTasks());
    }

}
