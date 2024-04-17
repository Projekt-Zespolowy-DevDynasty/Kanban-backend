package projektzespolowy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import projektzespolowy.models.SubTasks;

import java.util.List;

@Repository
public interface SubTasksRepository extends JpaRepository<SubTasks, Long> {

    List<SubTasks> findByTaskId(Long taskId);
}

