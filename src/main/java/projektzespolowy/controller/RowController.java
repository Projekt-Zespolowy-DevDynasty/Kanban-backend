package projektzespolowy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projektzespolowy.models.Card;
import projektzespolowy.models.RowWithAllCards;
import projektzespolowy.models.Task;
import projektzespolowy.repository.RowRepository;
import projektzespolowy.repository.CardRepository;
import projektzespolowy.repository.TaskRepository;
import projektzespolowy.wyjatki.ResourceNotFoundException;

import java.util.*;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/row")
public class RowController {

    private final RowRepository rowRepository;
    private final CardRepository cardRepository;
    private final TaskRepository taskRepository;

    @Autowired
    public RowController(TaskRepository taskRepository, RowRepository rowRepository, CardRepository cardRepository) {
        this.taskRepository = taskRepository;
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
            // sort rows by position
            List<RowWithAllCards> rows = rowRepository.findAll();
            for (RowWithAllCards row : rows) {
                row.getCardsinrow().sort(Comparator.comparingInt(Card::getPosition));
            }
            rows.sort(Comparator.comparingInt(RowWithAllCards::getPosition));
            return rows;
        }
    }
    @GetMapping("/{rowPosition}")
    private RowWithAllCards getRowByPosition(@PathVariable Integer rowPosition) {
        return rowRepository.findByPosition(rowPosition)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono wiersza o pozycji: " + rowPosition));
    }
    @GetMapping("/getrowbyid/{rowId}")
    private RowWithAllCards getRowById(@PathVariable Integer rowId) {
        // change rowid from integer to long
        Long rowIdLong = Long.valueOf(rowId);
        RowWithAllCards row = rowRepository.findById(rowIdLong)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono wiersza o identyfikatorze: " + rowId));
        row.getCardsinrow().sort(Comparator.comparingInt(Card::getPosition));
        return row;
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
    private ResponseEntity<RowWithAllCards> deleteRow(@PathVariable Long rowId) {
        RowWithAllCards row = rowRepository.findById(rowId)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono wiersza o identyfikatorze: " + rowId));


        List<Card> cardsToDelete = row.getCardsinrow();
        for (Card card : cardsToDelete) {
            cardRepository.delete(card);
        }

        rowRepository.delete(row);

        return ResponseEntity.ok().body(row);
    }
    @PutMapping("/{sourceRowId}/move-column/{sourceColumnPosition}/{targetColumnPosition}")
    private ResponseEntity<?> moveColumn(
            @PathVariable Long sourceRowId,
            @PathVariable Integer sourceColumnPosition,
            @PathVariable Integer targetColumnPosition) {


        RowWithAllCards sourceRow = rowRepository.findById(sourceRowId)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono wiersza o identyfikatorze: " + sourceRowId));


        List<RowWithAllCards> allRows = rowRepository.findAll();


        for (RowWithAllCards row : allRows) {

            List<Card> cardsInCurrentRow = row.getCardsinrow();


            for (Card card : cardsInCurrentRow) {

                if (card.getPosition() == sourceColumnPosition) {
                    card.setPosition(targetColumnPosition);
                }

                else if (sourceColumnPosition < targetColumnPosition && sourceColumnPosition < card.getPosition() && card.getPosition() <= targetColumnPosition) {
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

        return ResponseEntity.ok().body(row);
    }


    @PutMapping("/{rowId}/move-down")
    private ResponseEntity<RowWithAllCards> moveRowDown(@PathVariable Long rowId) {
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

        return ResponseEntity.ok().body(row);
    }
    @DeleteMapping("/remove-column/{columnPosition}")
    private ResponseEntity<?> removeColumnFromRow(@PathVariable Integer columnPosition) {

        List<RowWithAllCards> allRows = rowRepository.findAll();
        for (RowWithAllCards currentRow : allRows) {
            for (Card card : currentRow.getCardsinrow()) {
                if (card.getPosition() == columnPosition) {
                    Card leftCardwithout = currentRow.getCardsinrow().stream()
                            .filter(c -> c.getPosition() == columnPosition - 1)
                            .findFirst()
                            .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono karty o pozycji: " + (columnPosition - 1)));

                    Card leftCard = cardRepository.findById(leftCardwithout.getId())
                            .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono karty o identyfikatorze: " + leftCardwithout.getId()));


//                    List<Task> tasks = new ArrayList<>();
//
//                    for (Task task : card.getTasks()) {
//                        Task newTask = new Task();
//                        newTask.setName(task.getName());
//                        taskRepository.save(newTask);
//                        tasks.add(newTask);
//                    }
//                    List<Task> leftCardTasks = leftCard.getTasks();
//                    leftCardTasks.addAll(tasks);

                    List<Card> cardsInRow = currentRow.getCardsinrow();
                    for (Card card2 : cardsInRow) {
                        if (card2.getPosition() > columnPosition) {
                            card2.setPosition(card2.getPosition() - 1);
                            cardRepository.save(card2);
                        }
                    }
                    cardRepository.delete(card);
                    cardRepository.save(leftCard);
                    currentRow.getCardsinrow().remove(card);

                }
            }
            rowRepository.save(currentRow);
        }
        return ResponseEntity.ok().build();
    }




    //TODO: przenoszenie kolumny w wierszu, przenoszenie wiersza gora/dol, usuwanie kolumny, usuwanie wiersza, usuniecie useles funkcji z cardcontroler
}