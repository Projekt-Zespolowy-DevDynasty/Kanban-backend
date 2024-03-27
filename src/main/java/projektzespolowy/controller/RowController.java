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
        return rowRepository.findAll();
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
        List<Card>initialCards=new ArrayList<>();
        row.setPosition(newPosition);
        row.setCardsinrow(initialCards);


        RowWithAllCards createdRow = rowRepository.save(row);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdRow);
    }

    @PostMapping("/add-column/{rowId}")
    private ResponseEntity<RowWithAllCards> addColumnToRow(@PathVariable Long rowId, @RequestBody Card newCard) {

        Optional<RowWithAllCards> optionalRow = rowRepository.findById(rowId);
        if (optionalRow.isPresent()) {
            RowWithAllCards row = optionalRow.get();


            List<Card> existingCards = row.getCardsinrow();


            int newPosition = existingCards.size();
            newCard.setPosition(newPosition);


            existingCards.add(newCard);


            row.setCardsinrow(existingCards);


            RowWithAllCards updatedRow = rowRepository.save(row);

            return ResponseEntity.status(HttpStatus.CREATED).body(updatedRow);
        } else {

            return ResponseEntity.notFound().build();
        }
    }



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
}
