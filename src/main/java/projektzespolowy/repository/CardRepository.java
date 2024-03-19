package projektzespolowy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import projektzespolowy.models.Card;

import java.util.List;
import java.util.Optional;

public interface CardRepository extends JpaRepository<Card, Long> {
    Optional<Card> findByName(String toDo);

    Card findFirstByIdLessThanOrderByIdDesc(Long cardId);

    Optional<Card> findByPosition(int position);

    List<Card> findAllByPositionGreaterThan(int position);

    List<Card> findAllByPositionGreaterThanAndPositionLessThan(int position, int position1);
}