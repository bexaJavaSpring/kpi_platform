package uz.java.kpisystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.java.kpisystem.dto.taskTag.TaskTagResponse;
import uz.java.kpisystem.entity.Task;
import uz.java.kpisystem.entity.TaskTag;

import java.util.Collection;
import java.util.List;

public interface TaskTagRepository extends JpaRepository<TaskTag, Long> {
    List<TaskTag> findAllByTask(Task task);

    List<TaskTag> findAllByTaskId(Long taskId);
}
