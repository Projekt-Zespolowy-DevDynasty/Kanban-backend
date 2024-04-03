package projektzespolowy.repository;
import org.springframework.data.jpa.repository.JpaRepository;
import projektzespolowy.models.Card;
import projektzespolowy.models.Useer;
import java.util.List;
import java.util.Optional;
public interface UserRepository extends JpaRepository<Useer, Long> {
}
