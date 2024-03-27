package projektzespolowy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projektzespolowy.models.Card;
import projektzespolowy.models.RowWithAllCards;
import projektzespolowy.repository.RowRepository;
import projektzespolowy.repository.CardRepository;
import projektzespolowy.wyjatki.ResourceNotFoundException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/row")
public class RowController {

    private final RowRepository rowRepository;
    private final CardRepository cardRepository;

    @Autowired
    public RowController(RowRepository rowRepository, CardRepository cardRepository) {
        this.rowRepository = rowRepository;
        this.cardRepository = cardRepository;
    }

    @GetMapping("/all")
    private List<RowWithAllCards> getAllRows() {
        if (rowRepository.findAll().isEmpty()) {
            RowWithAllCards row = new RowWithAllCards();
            row.setPosition(0);
            List<Card> karty = cardRepository.findAll();
            Card kartaToDo = new Card("To do", Integer.MAX_VALUE, 0);
            Card kartaDone = new Card("Done", Integer.MAX_VALUE, 1);
            row.setCardsinrow(List.of(kartaDone, kartaToDo));
            rowRepository.save(row);
            return rowRepository.findAll();
        }else {
            return rowRepository.findAll();
        }
    }

    @GetMapping("/count")
    public Long getRowCount() {
        return rowRepository.count();
    }

    @PostMapping("/add")
    private ResponseEntity<RowWithAllCards> addRow() {
        RowWithAllCards lastRow = rowRepository.findTopByOrderByPositionDesc();
        RowWithAllCards row= new RowWithAllCards();

        int newPosition = 0;
        if (lastRow != null) {
            newPosition = lastRow.getPosition() + 1;
        }
        row.setPosition(newPosition);

        RowWithAllCards cardsoftoprow = rowRepository.findByPosition(0).orElseThrow(
                () -> new ResourceNotFoundException("Nie znaleziono wiersza o pozycji: 0")
        );
        List<Card> skopiujCards=new ArrayList<>();
        // kopiuje wszystkie kolumny z pierwszego wiersza do nowego wiersza
        for(Card kolumna: cardsoftoprow.getCardsinrow()){
            Card skopiujCard=new Card();
            skopiujCard.setName(kolumna.getName());
            skopiujCard.setPosition(kolumna.getPosition());
            skopiujCard.setMaxTasksLimit(kolumna.getMaxTasksLimit());
            cardRepository.save(skopiujCard);
            skopiujCards.add(skopiujCard);
        }
        row.setCardsinrow(skopiujCards);

        RowWithAllCards createdRow = rowRepository.save(row);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdRow);
    }

    @PostMapping("/add-column")
    private ResponseEntity<RowWithAllCards> addColumnToRow(@RequestBody String name) {
        Card newCard = new Card();
        newCard.setName(name);
        if(newCard.getName().equals("To do") || newCard.getName().equals("Done")){
            throw new UnsupportedOperationException("Nie można dodać karty o nazwie: " + newCard.getName());
        }
        if (newCard.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Nazwa karty nie może być pusta ani składać się wyłącznie z białych znaków.");
        }


        List<RowWithAllCards> rowsToUpdate=rowRepository.findAll();
        for(RowWithAllCards row: rowsToUpdate)
        {
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

// =============================================================================================================================================================

    @DeleteMapping("/{rowId}")
    private ResponseEntity<?> deleteRow(@PathVariable Long rowId) {
        RowWithAllCards row = rowRepository.findById(rowId)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono wiersza o identyfikatorze: " + rowId));


        List<Card> cardsToDelete = row.getCardsinrow();
        for (Card card : cardsToDelete) {
            cardRepository.delete(card);
        }

        rowRepository.delete(row);

        return ResponseEntity.ok().build();
    }
    @PutMapping("/{sourceRowId}/move-column/{columnId}/{targetRowId}")
    private ResponseEntity<?> moveColumn(
            @PathVariable Long sourceRowId,
            @PathVariable Long columnId,
            @PathVariable Long targetRowId) {

        RowWithAllCards sourceRow = rowRepository.findById(sourceRowId)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono wiersza o identyfikatorze: " + sourceRowId));
        RowWithAllCards targetRow = rowRepository.findById(targetRowId)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono wiersza o identyfikatorze: " + targetRowId));

        Card columnToMove = null;
        for (Card card : sourceRow.getCardsinrow()) {
            if (card.getId().equals(columnId)) {
                columnToMove = card;
                break;
            }
        }
        if (columnToMove == null) {
            throw new ResourceNotFoundException("Nie znaleziono kolumny o identyfikatorze: " + columnId);
        }

        sourceRow.getCardsinrow().remove(columnToMove);
        cardRepository.delete(columnToMove);

        columnToMove.setPosition(targetRow.getCardsinrow().size());
        cardRepository.save(columnToMove);
        targetRow.getCardsinrow().add(columnToMove);
        rowRepository.save(targetRow);

        return ResponseEntity.ok().build();
    }

    @PutMapping("/{rowId}/move-up")
    private ResponseEntity<?> moveRowUp(@PathVariable Long rowId) {
        RowWithAllCards row = rowRepository.findById(rowId)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono wiersza o identyfikatorze: " + rowId));


        if (row.getPosition() == 0) {
            throw new UnsupportedOperationException("Wiersz jest już na początku i nie może być przesunięty w górę.");
        }


        RowWithAllCards previousRow = rowRepository.findByPosition(row.getPosition() - 1)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono poprzedzającego wiersza."));


        int currentPosition = row.getPosition();
        row.setPosition(previousRow.getPosition());
        previousRow.setPosition(currentPosition);

        rowRepository.save(row);
        rowRepository.save(previousRow);

        return ResponseEntity.ok().build();
    }


    @PutMapping("/{rowId}/move-down")
    private ResponseEntity<?> moveRowDown(@PathVariable Long rowId) {
        RowWithAllCards row = rowRepository.findById(rowId)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono wiersza o identyfikatorze: " + rowId));


        if (row.getPosition() == rowRepository.count() - 1) {
            throw new UnsupportedOperationException("Wiersz jest już na końcu i nie może być przesunięty w dół.");
        }


        RowWithAllCards nextRow = rowRepository.findByPosition(row.getPosition() + 1)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono następującego wiersza."));


        int currentPosition = row.getPosition();
        row.setPosition(nextRow.getPosition());
        nextRow.setPosition(currentPosition);


        rowRepository.save(row);
        rowRepository.save(nextRow);

        return ResponseEntity.ok().build();
    }
    @DeleteMapping("/{rowId}/remove-column/{columnId}")
    private ResponseEntity<?> removeColumnFromRow(@PathVariable Long rowId, @PathVariable Long columnId) {
        RowWithAllCards row = rowRepository.findById(rowId)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono wiersza o identyfikatorze: " + rowId));

        Card columnToRemove = null;
        for (Card card : row.getCardsinrow()) {
            if (card.getId().equals(columnId)) {
                columnToRemove = card;
                break;
            }
        }
        if (columnToRemove == null) {
            throw new ResourceNotFoundException("Nie znaleziono kolumny o identyfikatorze: " + columnId);
        }

        row.getCardsinrow().remove(columnToRemove);
        cardRepository.delete(columnToRemove);
        rowRepository.save(row);

        return ResponseEntity.ok().build();
    }

    //TODO: przenoszenie kolumny w wierszu, przenoszenie wiersza gora/dol, usuwanie kolumny, usuwanie wiersza, usuniecie useles funkcji z cardcontroler
}
