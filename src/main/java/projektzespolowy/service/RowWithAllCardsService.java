package projektzespolowy.service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import projektzespolowy.DTO.RowWithAllCardsDTO;
import projektzespolowy.models.Card;
import projektzespolowy.models.RowWithAllCards;
import projektzespolowy.repository.CardRepository;
import projektzespolowy.repository.RowRepository;
import projektzespolowy.wyjatki.ResourceNotFoundException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RowWithAllCardsService {

    private final RowRepository rowRepository;
    private final CardRepository cardRepository;

    @Autowired
    public RowWithAllCardsService(RowRepository rowRepository, CardRepository cardRepository) {
        this.rowRepository = rowRepository;
        this.cardRepository = cardRepository;
    }

    public List<RowWithAllCards> getAllRows() {
        return rowRepository.findAll();
    }

    public RowWithAllCards getRowByPosition(Integer rowPosition) {
        return rowRepository.findByPosition(rowPosition)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono wiersza o pozycji: " + rowPosition));
    }
    public ResponseEntity<RowWithAllCardsDTO> addColumnToRow(String name) {

        if (name.equals("To do") || name.equals("Done")) {
            throw new UnsupportedOperationException("Nie można dodać karty o nazwie: " + name);
        }
        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nazwa karty nie może być pusta ani składać się wyłącznie z białych znaków.");
        }


        List<RowWithAllCards> rowsToUpdate = rowRepository.findAll();
        for (RowWithAllCards row : rowsToUpdate) {
            List<Card> cards = row.getCardsinrow();


            Card doneCard = cards.stream()
                    .filter(c -> c.getName().equals("Done"))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono karty o nazwie: Done"));


            Card card = new Card();
            card.setPosition(doneCard.getPosition());
            doneCard.setPosition(doneCard.getPosition() + 1);


            cardRepository.save(doneCard);
            card.setMaxTasksLimit(5);
            card.setName(name);
            cardRepository.save(card);
            row.getCardsinrow().add(card);
            rowRepository.save(row);
        }


        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

    public RowWithAllCards getRowById(Long rowId) {
        return rowRepository.findById(rowId)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono wiersza o identyfikatorze: " + rowId));
    }

    public Long getRowCount() {
        return rowRepository.count();
    }

    public RowWithAllCards addRow(String name) {
        RowWithAllCards lastRow = rowRepository.findTopByOrderByPositionDesc();
        RowWithAllCards row = new RowWithAllCards();

        int newPosition = 0;
        if (lastRow != null) {
            newPosition = lastRow.getPosition() + 1;
        }
        row.setPosition(newPosition);

        RowWithAllCards cardsoftoprow = rowRepository.findByPosition(0).orElseThrow(
                () -> new ResourceNotFoundException("Nie znaleziono wiersza o pozycji: 0")
        );
        List<Card> copiedCards = new ArrayList<>();

        for (Card column : cardsoftoprow.getCardsinrow()) {
            Card copiedCard = new Card();
            copiedCard.setName(column.getName());
            copiedCard.setPosition(column.getPosition());
            copiedCard.setMaxTasksLimit(column.getMaxTasksLimit());
            cardRepository.save(copiedCard);
            copiedCards.add(copiedCard);
        }
        row.setCardsinrow(copiedCards);

        // Ustawienie nazwy nowego wiersza
        row.setName(name);
        return rowRepository.save(row);
    }

    public void removeRow(Long rowId) {
        RowWithAllCards row = getRowById(rowId);

        // Pobierz listę kart, które należy usunąć
        List<Card> cardsToDelete = row.getCardsinrow();
        for (Card card : cardsToDelete) {
            // Usuń każdą kartę
            cardRepository.delete(card);
        }

        // Usuń wiersz
        rowRepository.delete(row);
    }

    public RowWithAllCards renameRow(Long rowId, String newName) {
        RowWithAllCards row = getRowById(rowId);
        row.setName(newName);
        return rowRepository.save(row);
    }
    public void moveColumn(Integer sourceColumnPosition, Integer targetColumnPosition) {
        // Pobierz wszystkie wiersze
        List<RowWithAllCards> allRows = rowRepository.findAll();

        // Iteruj przez każdy wiersz
        for (RowWithAllCards row : allRows) {
            List<Card> cardsInCurrentRow = row.getCardsinrow();

            // Znajdź kartę o pozycji źródłowej i przenieś ją na pozycję docelową
            for (Card card : cardsInCurrentRow) {
                if (card.getPosition() == sourceColumnPosition) {
                    card.setPosition(targetColumnPosition);
                } else if (sourceColumnPosition < targetColumnPosition && sourceColumnPosition < card.getPosition() && card.getPosition() <= targetColumnPosition) {
                    card.setPosition(card.getPosition() - 1);
                } else if (targetColumnPosition < sourceColumnPosition && targetColumnPosition <= card.getPosition() && card.getPosition() < sourceColumnPosition) {
                    card.setPosition(card.getPosition() + 1);
                }
            }
        }

        // Zapisz zmiany w bazie danych
        rowRepository.saveAll(allRows);
    }

    public RowWithAllCards moveRowUp(Long rowId) {
        RowWithAllCards row = rowRepository.findById(rowId)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono wiersza o identyfikatorze: " + rowId));

        if (row.getPosition() == 1) {
            throw new UnsupportedOperationException("Wiersz jest już na początku i nie może być przesunięty w górę.");
        }

        // Pobierz poprzedni wiersz
        RowWithAllCards previousRow = rowRepository.findByPosition(row.getPosition() - 1)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono poprzedniego wiersza."));

        // Zamień pozycje obecnego wiersza i poprzedniego wiersza
        int currentPosition = row.getPosition();
        row.setPosition(previousRow.getPosition());
        previousRow.setPosition(currentPosition);

        // Zapisz zmiany w bazie danych
        rowRepository.save(row);
        rowRepository.save(previousRow);

        return row;
    }

    public RowWithAllCards moveRowDown(Long rowId) {
        RowWithAllCards row = rowRepository.findById(rowId)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono wiersza o identyfikatorze: " + rowId));

        if (row.getPosition() == rowRepository.count() - 1) {
            throw new UnsupportedOperationException("Wiersz jest już na końcu i nie może być przesunięty w dół.");
        }

        // Pobierz następny wiersz
        RowWithAllCards nextRow = rowRepository.findByPosition(row.getPosition() + 1)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono następnego wiersza."));

        // Zamień pozycje obecnego wiersza i następnego wiersza
        int currentPosition = row.getPosition();
        row.setPosition(nextRow.getPosition());
        nextRow.setPosition(currentPosition);

        // Zapisz zmiany w bazie danych
        rowRepository.save(row);
        rowRepository.save(nextRow);

        return row;
    }

    public void removeColumnAndAdjust(int position) {
        // Pobierz wszystkie wiersze
        List<RowWithAllCards> allRows = rowRepository.findAll();

        // Iteruj przez każdy wiersz
        for (RowWithAllCards row : allRows) {
            List<Card> cardsInCurrentRow = row.getCardsinrow();

            // Usuń kartę na podanej pozycji
            for (Card card : cardsInCurrentRow) {
                if (card.getPosition() == position) {
                    cardRepository.delete(card);
                    break;
                }
            }

            // Dostosuj pozycje kart w wierszu
            cardsInCurrentRow.removeIf(card -> card.getPosition() == position);
            for (Card card : cardsInCurrentRow) {
                if (card.getPosition() > position) {
                    card.setPosition(card.getPosition() - 1);
                }
            }
        }

        // Zapisz zmiany w bazie danych
        rowRepository.saveAll(allRows);
    }


}
