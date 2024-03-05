package projektzespolowy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import projektzespolowy.models.Task;

public interface TaskRepository extends JpaRepository<Task, Long>{
}
