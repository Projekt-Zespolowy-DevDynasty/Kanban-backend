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

    private final RowWithAllCardsService rowService;

    @Autowired
    public RowController(RowWithAllCardsService rowService) {
        this.rowService = rowService;
    }

    @GetMapping("/all")
    public ResponseEntity<List<RowWithAllCards>> getAllRows() {
        List<RowWithAllCards> rows = rowService.getAllRows();
        return ResponseEntity.ok(rows);
    }

    @GetMapping("/{rowPosition}")
    public ResponseEntity<RowWithAllCards> getRowByPosition(@PathVariable Integer rowPosition) {
        RowWithAllCards row = rowService.getRowByPosition(rowPosition);
        return ResponseEntity.ok(row);
    }

    @GetMapping("/getrowbyid/{rowId}")
    public ResponseEntity<RowWithAllCards> getRowById(@PathVariable Long rowId) {
        RowWithAllCards row = rowService.getRowById(rowId);
        return ResponseEntity.ok(row);
    }

    @GetMapping("/count")
    public Long getRowCount() {
        return rowService.getRowCount();
    }

    @PostMapping("/add")
    public ResponseEntity<RowWithAllCards> addRow(@RequestBody String name) {
        RowWithAllCards newRow = rowService.addRow(name);
        return ResponseEntity.status(HttpStatus.CREATED).body(newRow);
    }

    @DeleteMapping("/{rowId}")
    public ResponseEntity<Void> deleteRow(@PathVariable Long rowId) {
        rowService.removeRow(rowId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/rename-row/{rowId}")
    public ResponseEntity<RowWithAllCards> renameRow(@PathVariable Long rowId, @RequestBody String newName) {
        RowWithAllCards renamedRow = rowService.renameRow(rowId, newName);
        return ResponseEntity.ok(renamedRow);
    }

    @PutMapping("/{rowId}/move-up")
    public ResponseEntity<RowWithAllCards> moveRowUp(@PathVariable Long rowId) {
        RowWithAllCards movedRow = rowService.moveRowUp(rowId);
        return ResponseEntity.ok(movedRow);
    }

    @PutMapping("/{rowId}/move-down")
    public ResponseEntity<RowWithAllCards> moveRowDown(@PathVariable Long rowId) {
        RowWithAllCards movedRow = rowService.moveRowDown(rowId);
        return ResponseEntity.ok(movedRow);
    }

    @DeleteMapping("/remove-column/{position}")
    public ResponseEntity<Void> removeColumnAndAdjust(@PathVariable int position) {
        rowService.removeColumnAndAdjust(position);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/move-column/{sourceColumnPosition}/{targetColumnPosition}")
    public ResponseEntity<Void> moveColumn(@PathVariable Integer sourceColumnPosition, @PathVariable Integer targetColumnPosition) {
        rowService.moveColumn(sourceColumnPosition, targetColumnPosition);
        return ResponseEntity.ok().build();
    }
}