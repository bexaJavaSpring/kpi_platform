package uz.java.kpisystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.java.kpisystem.entity.Task;
import uz.java.kpisystem.entity.TaskMember;

import java.util.List;

public interface TaskMemberRepository extends JpaRepository<TaskMember, Long> {
    List<TaskMember> findAllByTask(Task task);

}
