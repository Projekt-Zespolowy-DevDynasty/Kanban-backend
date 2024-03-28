package projektzespolowy.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import projektzespolowy.models.Card;
import projektzespolowy.models.RowWithAllCards;
import projektzespolowy.models.Task;
import projektzespolowy.repository.RowRepository;
import projektzespolowy.repository.TaskRepository;
import projektzespolowy.wyjatki.ResourceNotFoundException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class RowWithAllCardsService {

    @Autowired
    private RowRepository rowWithAllCardsRepository;
    @Autowired
    private TaskRepository taskRepository;

    @Transactional
    public void removeColumnAndAdjust(int position) {
        List<RowWithAllCards> rows = rowWithAllCardsRepository.findAll();

        for (RowWithAllCards row : rows) {
            row.getCardsinrow().sort((c1, c2) -> Integer.compare(c1.getPosition(), c2.getPosition()));
            Card toRemove = null;
            for (int i = 0; i < row.getCardsinrow().size(); i++) {
                Card card = row.getCardsinrow().get(i);
                if (card.getPosition() == position) {
                    toRemove = card;
                    // TODO: nie kopiowac taskow tylko caly obiekt task przeniesc do karty po lewej
                    // Skopiuj i przenieś taski do karty po lewej, jeśli istnieje
                    if (i > 0) {
                        Card leftCard = row.getCardsinrow().get(i - 1);
                        List<Task> newTasks = new ArrayList<>();
                        for (Task task : card.getTasks()) {
                            Task newTask = new Task(); // Załóżmy, że konstruktor Task przyjmuje te parametry
                            newTask.setName(task.getName());
                            taskRepository.save(newTask); // Zapisz nowy task
                            newTasks.add(newTask);
                        }
                        leftCard.getTasks().addAll(newTasks); // Dodaj nowe taski do karty po lewej
                    }
                } else if (card.getPosition() > position) {
                    card.setPosition(card.getPosition() - 1);
                }
            }
            if (toRemove != null) {
                row.getCardsinrow().remove(toRemove);
            }
        }

        rowWithAllCardsRepository.saveAll(rows);
    }
}