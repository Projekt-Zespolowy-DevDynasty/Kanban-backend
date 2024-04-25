package projektzespolowy.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.*;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Optional;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import projektzespolowy.models.RowWithAllCards;
import projektzespolowy.models.Card;
import projektzespolowy.repository.RowRepository;
import projektzespolowy.repository.CardRepository;
import projektzespolowy.service.RowWithAllCardsService;
import projektzespolowy.wyjatki.ResourceNotFoundException;

public class RowWithAllCardsServiceTest {

    @Mock
    private RowRepository rowRepository;

    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private RowWithAllCardsService rowWithAllCardsService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetAllRows() {

        RowWithAllCards row1 = new RowWithAllCards();
        RowWithAllCards row2 = new RowWithAllCards();
        when(rowRepository.findAll()).thenReturn(Arrays.asList(row1, row2));


        List<RowWithAllCards> result = rowWithAllCardsService.getAllRows();


        assertEquals(2, result.size());
    }

    @Test
    public void testGetRowByPosition() {

        Integer position = 1;
        RowWithAllCards row = new RowWithAllCards();
        when(rowRepository.findByPosition(position)).thenReturn(Optional.of(row));


        RowWithAllCards result = rowWithAllCardsService.getRowByPosition(position);


        assertEquals(row, result);
    }

    @Test
    void testAddRow() {

        String newRowName = "New Row";
        RowWithAllCards lastRow = new RowWithAllCards();
        lastRow.setPosition(1);
        List<Card> cardsInTopRow = new ArrayList<>();
        Card card = new Card();
        card.setName("Test Card");
        card.setPosition(0);
        card.setMaxTasksLimit(10);
        cardsInTopRow.add(card);
        RowWithAllCards topRow = new RowWithAllCards();
        topRow.setPosition(0);
        topRow.setCardsinrow(cardsInTopRow);


        Mockito.when(rowRepository.save(Mockito.any(RowWithAllCards.class))).thenAnswer(invocation -> {
            RowWithAllCards savedRow = invocation.getArgument(0);
            savedRow.setId(1L);
            return savedRow;
        });
        Mockito.when(rowRepository.findTopByOrderByPositionDesc()).thenReturn(lastRow);
        Mockito.when(rowRepository.findByPosition(0)).thenReturn(Optional.of(topRow));


        RowWithAllCardsService rowService = new RowWithAllCardsService(rowRepository, cardRepository);


        RowWithAllCards newRow = rowService.addRow(newRowName);


        Assertions.assertNotNull(newRow);
        Assertions.assertEquals(newRowName, newRow.getName());
        Assertions.assertEquals(2, newRow.getPosition()); // W zależności od danych testowych
        Assertions.assertEquals(1, newRow.getCardsinrow().size()); // Oczekiwana liczba kart w nowym wierszu


        Card copiedCard = newRow.getCardsinrow().get(0);
        Assertions.assertEquals("Test Card", copiedCard.getName());
        Assertions.assertEquals(0, copiedCard.getPosition());
        Assertions.assertEquals(10, copiedCard.getMaxTasksLimit());
    }


    @Test
    void testRemoveRow() {

        Long rowIdToRemove = 1L;
        RowWithAllCards rowToRemove = new RowWithAllCards();
        rowToRemove.setId(rowIdToRemove);
        List<Card> cardsInRow = new ArrayList<>();
        Card card1 = new Card();
        card1.setId(1L);
        Card card2 = new Card();
        card2.setId(2L);
        cardsInRow.add(card1);
        cardsInRow.add(card2);
        rowToRemove.setCardsinrow(cardsInRow);


        Mockito.when(rowRepository.findById(rowIdToRemove)).thenReturn(Optional.of(rowToRemove));


        rowWithAllCardsService.removeRow(rowIdToRemove);


        Mockito.verify(cardRepository, Mockito.times(2)).delete(Mockito.any(Card.class));


        Mockito.verify(rowRepository, Mockito.times(1)).delete(rowToRemove);
    }

    @Test
    public void testRenameRow() {

        Long rowId = 1L;
        String newName = "New Row Name";
        RowWithAllCards row = new RowWithAllCards();
        row.setId(rowId);
        row.setName("Old Row Name");


        when(rowRepository.findById(rowId)).thenReturn(Optional.of(row));
        when(rowRepository.save(any(RowWithAllCards.class))).thenAnswer(invocation -> invocation.getArgument(0)); // mockowanie save do zwracania tego, co otrzymał
        RowWithAllCards updatedRow = rowWithAllCardsService.renameRow(rowId, newName);


        verify(rowRepository).findById(rowId);
        verify(rowRepository).save(row);
        assertEquals(newName, updatedRow.getName());
    }

    @Test
    public void testMoveColumn() {

        Integer sourceColumnPosition = 1;
        Integer targetColumnPosition = 2;
        RowWithAllCards row1 = new RowWithAllCards();
        Card card1 = new Card();
        card1.setPosition(sourceColumnPosition);
        row1.setCardsinrow(Arrays.asList(card1));
        when(rowRepository.findAll()).thenReturn(Arrays.asList(row1));


        assertDoesNotThrow(() -> rowWithAllCardsService.moveColumn(sourceColumnPosition, targetColumnPosition));


        assertEquals(targetColumnPosition, row1.getCardsinrow().get(0).getPosition());
    }

    @Test
    public void testMoveRowUp() {

        Long rowId = 2L;
        RowWithAllCards row1 = new RowWithAllCards();
        RowWithAllCards row2 = new RowWithAllCards();
        row1.setPosition(1);
        row2.setPosition(2);
        when(rowRepository.findById(rowId)).thenReturn(Optional.of(row2));
        when(rowRepository.findByPosition(1)).thenReturn(Optional.of(row1));


        RowWithAllCards result = rowWithAllCardsService.moveRowUp(rowId);


        assertEquals(1, result.getPosition());
        assertEquals(2, row1.getPosition());
    }

    @Test
    public void testMoveRowDown() {

        Long rowId = 1L;
        RowWithAllCards row1 = new RowWithAllCards();
        RowWithAllCards row2 = new RowWithAllCards();
        row1.setPosition(1);
        row2.setPosition(2);
        when(rowRepository.findById(rowId)).thenReturn(Optional.of(row1));
        when(rowRepository.findByPosition(2)).thenReturn(Optional.of(row2));


        RowWithAllCards result = rowWithAllCardsService.moveRowDown(rowId);


        assertEquals(2, result.getPosition());
        assertEquals(1, row2.getPosition());
    }

    @Test
    void testRemoveColumnAndAdjust() {

        List<RowWithAllCards> allRows = new ArrayList<>();
        RowWithAllCards row1 = new RowWithAllCards();
        row1.setId(1L);
        row1.setPosition(1);
        List<Card> cardsInRow1 = new ArrayList<>();
        Card card1 = new Card();
        card1.setId(1L);
        card1.setPosition(1);
        Card card2 = new Card();
        card2.setId(2L);
        card2.setPosition(2);
        cardsInRow1.add(card1);
        cardsInRow1.add(card2);
        row1.setCardsinrow(cardsInRow1);
        allRows.add(row1);


        when(rowRepository.findAll()).thenReturn(allRows);
        doNothing().when(cardRepository).delete(any());
        when(cardRepository.save(any())).thenReturn(null);


        RowWithAllCardsService rowWithAllCardsService = new RowWithAllCardsService(rowRepository, cardRepository);
        rowWithAllCardsService.removeColumnAndAdjust(1);


        verify(cardRepository, times(2)).delete(any()); // Two cards are deleted
        verify(cardRepository, times(2)).save(any());   // Two cards are saved
    }
}
