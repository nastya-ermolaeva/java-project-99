package hexlet.code.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

import hexlet.code.model.Task;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    Optional<Task> findByName(String name);
    boolean existsByAssigneeId(Long assigneeId);
    boolean existsByTaskStatusId(Long taskStatusId);
    boolean existsByLabelsId(Long labelId);
}

