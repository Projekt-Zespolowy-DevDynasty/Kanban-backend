package projektzespolowy.controller;

import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import projektzespolowy.DTO.CardDTO;
import projektzespolowy.models.Card;
import projektzespolowy.models.Task;
import projektzespolowy.models.RowWithAllCards;
import projektzespolowy.repository.CardRepository;
import projektzespolowy.repository.RowRepository;
import projektzespolowy.repository.TaskRepository;
import projektzespolowy.service.CardServiceImpl;
import projektzespolowy.wyjatki.ResourceNotFoundException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/card")
public class CardController {
    private final CardServiceImpl cardService;

    @Autowired
    public CardController(CardServiceImpl cardService) {
        this.cardService = cardService;

    }

    @GetMapping("/{id}")
    private ResponseEntity<CardDTO> getCard(@PathVariable Long id) {
        CardDTO cardDto = CardDTO.from(cardService.getCard(id));
        return ResponseEntity.ok(cardDto);
    }


    @Operation(summary = "Zwraca wszystkie Karty")
    @GetMapping("/all")
    private ResponseEntity<List<CardDTO>> getAllCards() {
        List<Card> cards = cardService.getAllCards();
        List<CardDTO> cardDTOs = cards.stream()
                .map(CardDTO::from)
                .collect(Collectors.toList());
        return ResponseEntity.ok(cardDTOs);
    }


    @PostMapping("/add")
    private ResponseEntity<CardDTO> addCard(@RequestBody Card card) {
        Card savedCard = cardService.addCard(CardDTO.from(card));
        return new ResponseEntity<>(CardDTO.from(savedCard), HttpStatus.CREATED);
    }


    @DeleteMapping("/{cardId}/task/{taskId}")
    public void deleteTaskFromCard(@PathVariable Long cardId, @PathVariable Long taskId) {
        cardService.deleteTaskFromCard(cardId, taskId);
    }

    @PutMapping("/{id}/maxTasksLimit")
    public ResponseEntity<Void> updateMaxTasksLimit(@PathVariable Long id, @RequestBody int maxTasksLimit) {
        cardService.updateMaxTasksLimit(id, maxTasksLimit);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/{id}/edit-name")
    public ResponseEntity<Void> editColumnName(@PathVariable Long id, @RequestBody String newName) {
        cardService.editColumnName(id, newName);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/{destinationId}/position/{sourceId}")
    public ResponseEntity<Void> updateCardPosition(@PathVariable Long destinationId, @PathVariable Long sourceId) {
        cardService.updateCardPosition(destinationId, sourceId);
        return ResponseEntity.noContent().build();
    }
    @PutMapping("/{sourceCardId}/move-task/{taskId}/to-card/{destinationCardId}/at-index/{index}")
    private void moveTaskToAnotherCard(@PathVariable Long sourceCardId, @PathVariable Long taskId,
                                       @PathVariable Long destinationCardId, @PathVariable int index) {
        cardService.moveTask(sourceCardId, taskId, destinationCardId, index);
    }


}
