package projektzespolowy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projektzespolowy.DTO.RowWithAllCardsDTO;
import projektzespolowy.models.Card;
import projektzespolowy.models.RowWithAllCards;
import projektzespolowy.repository.RowRepository;
import projektzespolowy.repository.CardRepository;
import projektzespolowy.repository.TaskRepository;
import projektzespolowy.service.RowWithAllCardsService;
import projektzespolowy.wyjatki.ResourceNotFoundException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/row")
public class RowController {

    private final RowRepository rowRepository;
    private final CardRepository cardRepository;
    private final TaskRepository taskRepository;
    private final RowWithAllCardsService rowWithAllCardsService;

    @Autowired
    public RowController(TaskRepository taskRepository, RowRepository rowRepository, CardRepository cardRepository, RowWithAllCardsService rowWithAllCardsService) {
        this.taskRepository = taskRepository;
        this.rowRepository = rowRepository;
        this.cardRepository = cardRepository;
        this.rowWithAllCardsService = rowWithAllCardsService;
    }

    @GetMapping("/all")
    private ResponseEntity<List<RowWithAllCardsDTO>> getAllRows() {
        List<RowWithAllCards> rows = rowWithAllCardsService.getAllRows();
        List<RowWithAllCardsDTO> rowDTOs = rows.stream()
                .map(RowWithAllCardsDTO::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(rowDTOs);
    }
    @GetMapping("/{rowPosition}")
    private ResponseEntity<RowWithAllCardsDTO> getRowByPosition(@PathVariable Integer rowPosition) {
        RowWithAllCards row = rowRepository.findByPosition(rowPosition)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono wiersza o pozycji: " + rowPosition));
        RowWithAllCardsDTO rowDTO = RowWithAllCardsDTO.from(row);
        return ResponseEntity.ok(rowDTO);
    }
    @GetMapping("/getrowbyid/{rowId}")
    private ResponseEntity<RowWithAllCardsDTO> getRowById(@PathVariable Long rowId) {
        RowWithAllCards row = rowRepository.findById(rowId)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono wiersza o identyfikatorze: " + rowId));
        row.getCardsinrow().sort(Comparator.comparingInt(Card::getPosition));
        RowWithAllCardsDTO rowDTO = RowWithAllCardsDTO.from(row);
        return ResponseEntity.ok(rowDTO);
    }
    @PutMapping("/rename-row/{rowId}")
    private ResponseEntity<?> renameRow(@PathVariable Long rowId, @RequestBody String newName) {
        RowWithAllCards row = rowRepository.findById(rowId)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono wiersza o identyfikatorze: " + rowId));

        row.setName(newName);
        rowRepository.save(row);

        return ResponseEntity.ok().build();
    }
    @GetMapping("/count")
    public Long getRowCount() {
        return rowRepository.count();
    }
    @PostMapping("/add")
    private ResponseEntity<RowWithAllCardsDTO> addRow(@RequestBody String name) {
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
        List<Card> skopiujCards = new ArrayList<>();

        for (Card kolumna : cardsoftoprow.getCardsinrow()) {
            Card skopiujCard = new Card();
            skopiujCard.setName(kolumna.getName());
            skopiujCard.setPosition(kolumna.getPosition());
            skopiujCard.setMaxTasksLimit(kolumna.getMaxTasksLimit());
            cardRepository.save(skopiujCard);
            skopiujCards.add(skopiujCard);
        }
        row.setCardsinrow(skopiujCards);

        // Ustawienie nazwy nowego wiersza
        row.setName(name);
        RowWithAllCards createdRow = rowRepository.save(row);

        RowWithAllCardsDTO createdRowDTO = RowWithAllCardsDTO.from(createdRow);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdRowDTO);
    }
    @PostMapping("/add-column")
    private ResponseEntity<RowWithAllCardsDTO> addColumnToRow(@RequestBody String name) {
        Card newCard = new Card();
        newCard.setName(name);

        // Sprawdź, czy nazwa nowej kolumny jest prawidłowa
        if(newCard.getName().equals("To do") || newCard.getName().equals("Done")){
            throw new UnsupportedOperationException("Nie można dodać karty o nazwie: " + newCard.getName());
        }
        if (newCard.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Nazwa karty nie może być pusta ani składać się wyłącznie z białych znaków.");
        }

        // Pobierz wszystkie wiersze, aby dodać nową kolumnę do każdego z nich
        List<RowWithAllCards> rowsToUpdate = rowRepository.findAll();
        for (RowWithAllCards row : rowsToUpdate) {
            List<Card> cards = row.getCardsinrow();

            // Znajdź kartę "Done", aby ustawić pozycję nowej kolumny
            Card doneCard = cards.stream()
                    .filter(c -> c.getName().equals("Done"))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono karty o nazwie: Done"));

            // Ustaw pozycję nowej kolumny
            Card card = new Card();
            card.setPosition(doneCard.getPosition());
            doneCard.setPosition(doneCard.getPosition() + 1);

            // Zapisz zmiany
            cardRepository.save(doneCard);
            card.setMaxTasksLimit(5);
            card.setName(name);
            cardRepository.save(card);
            row.getCardsinrow().add(card);
            rowRepository.save(row);
        }

        // Zwróć odpowiedź z kodem 201 CREATED oraz wartością null, ponieważ nie ma potrzeby zwracać ciała odpowiedzi
        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }
    @DeleteMapping("/{rowId}")
    private ResponseEntity<RowWithAllCardsDTO> deleteRow(@PathVariable Long rowId) {
        // Znajdź wiersz do usunięcia na podstawie podanego identyfikatora
        RowWithAllCards row = rowRepository.findById(rowId)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono wiersza o identyfikatorze: " + rowId));

        // Pobierz listę kart, które należy usunąć
        List<Card> cardsToDelete = row.getCardsinrow();
        for (Card card : cardsToDelete) {
            // Usuń każdą kartę
            cardRepository.delete(card);
        }

        // Usuń wiersz
        rowRepository.delete(row);

        // Zwróć odpowiedź z kodem 200 OK oraz usuniętym wierszem
        return ResponseEntity.ok().body(RowWithAllCardsDTO.from(row));
    }
    @PutMapping("/move-column/{sourceColumnPosition}/{targetColumnPosition}")
    private ResponseEntity<?> moveColumn(
            @PathVariable Integer sourceColumnPosition,
            @PathVariable Integer targetColumnPosition) {

        List<RowWithAllCards> allRows = rowRepository.findAll();

        for (RowWithAllCards row : allRows) {
            List<Card> cardsInCurrentRow = row.getCardsinrow();

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

        rowRepository.saveAll(allRows);

        return ResponseEntity.ok().build();
    }
    @PutMapping("/{rowId}/move-up")
    private ResponseEntity<RowWithAllCards> moveRowUp(@PathVariable Long rowId) {
        RowWithAllCards row = rowRepository.findById(rowId)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono wiersza o identyfikatorze: " + rowId));

        if (row.getPosition() == 1) {
            throw new UnsupportedOperationException("Wiersz jest już na początku i nie może być przesunięty w górę.");
        }

        RowWithAllCards previousRow = rowRepository.findByPosition(row.getPosition() - 1)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono poprzedzającego wiersza."));

        int currentPosition = row.getPosition();
        row.setPosition(previousRow.getPosition());
        previousRow.setPosition(currentPosition);

        rowRepository.save(row);
        rowRepository.save(previousRow);

        return ResponseEntity.ok().body(row);
    }
    @PutMapping("/{rowId}/move-down")
    private ResponseEntity<RowWithAllCardsDTO> moveRowDown(@PathVariable Long rowId) {
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

        return ResponseEntity.ok().body(RowWithAllCardsDTO.from(row));
    }
    @DeleteMapping("/remove-column/{position}")
    public void removeColumnAndAdjust(@PathVariable int position) {
        rowWithAllCardsService.removeColumnAndAdjust(position);
    }



}