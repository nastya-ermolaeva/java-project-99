package hexlet.code.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

import hexlet.code.model.Task;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    Optional<Task> findByName(String name);
    List<Task> findByAssigneeId(Long assigneeId);
    List<Task> findByTaskStatusSlug(String taskStatusSlug);
}

