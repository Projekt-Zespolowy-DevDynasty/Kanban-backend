package projektzespolowy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import projektzespolowy.models.Card;

import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {
    Optional<Card> findByName(String toDo);
}