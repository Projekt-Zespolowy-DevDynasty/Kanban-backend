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

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRow(@PathVariable Long id) {
        RowWithAllCards rowToDelete = rowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono wiersza o podanym ID: " + id));
        List<RowWithAllCards> rowsToUpdate=rowRepository.findAllByPositionGreaterThanOrderByPositionAsc(rowToDelete.getPosition());
                for(RowWithAllCards row:rowsToUpdate)
                {
                    row.setPosition(row.getPosition()-1);
                    rowRepository.save(row);
                }
        rowRepository.delete(rowToDelete);
    }
    //TODO: przenoszenie kolumny w wierszu, przenoszenie wiersza gora/dol, usuwanie kolumny, usuwanie wiersza, usuniecie useles funkcji z cardcontroler
}
