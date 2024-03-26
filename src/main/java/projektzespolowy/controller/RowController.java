package projektzespolowy.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projektzespolowy.models.Row;
import projektzespolowy.models.Card;
import projektzespolowy.repository.RowRepository;
import projektzespolowy.repository.CardRepository;
import projektzespolowy.wyjatki.ResourceNotFoundException;

import java.util.List;

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
    private List<Row> getAllRows() {
        return rowRepository.findAll();
    }

    @PostMapping("/add")
    private ResponseEntity<Row> addRow() {

        Row lastRow = rowRepository.findTopByOrderByPositionDesc();
        Row row= new Row();

        int newPosition = 1;
        if (lastRow != null) {

            newPosition = lastRow.getPosition() + 1;
        }


        row.setPosition(newPosition);

        Row createdRow = rowRepository.save(row);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdRow);
    }


    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteRow(@PathVariable Long id) {
        Row rowToDelete = rowRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Nie znaleziono wiersza o podanym ID: " + id));
        List<Row> rowsToUpdate=rowRepository.findAllByPositionGreaterThanOrderByPositionAsc(rowToDelete.getPosition());
                for(Row row:rowsToUpdate)
                {
                    row.setPosition(row.getPosition()-1);
                    rowRepository.save(row);
                }
        rowRepository.delete(rowToDelete);
    }
}
