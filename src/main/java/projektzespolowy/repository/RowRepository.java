package projektzespolowy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import projektzespolowy.models.Card;
import projektzespolowy.models.RowWithAllCards;

import java.util.List;
import java.util.Optional;

@Repository
public interface RowRepository extends JpaRepository<RowWithAllCards, Long> {

    Optional<RowWithAllCards> findByPosition(int i);

    List<RowWithAllCards> findAllByPositionGreaterThanOrderByPositionAsc(int position);

    RowWithAllCards findTopByOrderByPositionDesc();
}
