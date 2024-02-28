package projektzespolowy.devdynasty.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import projektzespolowy.devdynasty.models.Board;

public interface BoardRepository extends JpaRepository<Board, Long> {
}