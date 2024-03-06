package projektzespolowy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import projektzespolowy.models.Card;

public interface CardRepository extends JpaRepository<Card, Long> {
}