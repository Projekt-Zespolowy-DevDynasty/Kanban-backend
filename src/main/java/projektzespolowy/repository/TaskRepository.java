package projektzespolowy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import projektzespolowy.models.Task;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long>{
    @Query("SELECT t FROM Task t JOIN t.useers u WHERE u.id = :userId")
    List<Task> findAllWithUser(Long userId);
}
