package projektzespolowy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import projektzespolowy.models.Row;

import java.util.List;
import java.util.Optional;

@Repository
public interface RowRepository extends JpaRepository<Row, Long> {

    Optional<Object> findByPosition(int i);

    List<Row> findAllByPositionGreaterThanOrderByPositionAsc(int position);

    Row findTopByOrderByPositionDesc();
}
