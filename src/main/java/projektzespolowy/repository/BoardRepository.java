package projektzespolowy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import projektzespolowy.models.Board;

public interface BoardRepository extends JpaRepository<Board, Long> {
}