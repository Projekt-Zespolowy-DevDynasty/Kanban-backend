package projektzespolowy.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.extension.ExtendWith;
import projektzespolowy.models.Card;
import projektzespolowy.repository.CardRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;



@ExtendWith(MockitoExtension.class)
class RowWithAllCardsServiceTest {
    @Mock
    private CardRepository cardRepository;

    @InjectMocks
    private CardServiceImpl cardService; // Założenie, że CardServiceImpl implementuje CardService

    @Test
    public void testGetAllCards() {
        Card existingCard1 = new Card("Test Card 1", 5, 2);
        Card existingCard2 = new Card("Test Card 2", 5, 3);

        when(cardRepository.findAll()).thenReturn(Arrays.asList(existingCard1, existingCard2));
        when(cardRepository.findByName("To do")).thenReturn(Optional.empty());
        when(cardRepository.findByName("Done")).thenReturn(Optional.empty());

        List<Card> result = cardService.getAllCards();

        assertNotNull(result);
        assertEquals(4, result.size());
        assertEquals("To do", result.get(0).getName());
        assertEquals("Done", result.get(result.size() - 1).getName());

        verify(cardRepository, times(2)).save(any(Card.class)); // Dwa razy, dla "To do" i "Done"
    }
}