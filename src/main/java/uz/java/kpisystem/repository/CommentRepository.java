package uz.java.kpisystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import uz.java.kpisystem.entity.Comment;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long>, JpaSpecificationExecutor<Comment> {

//    @Query("select t from Comment t join fetch t.task") // HQL(Hibernate Query Language
    @Query(value = "select c from comments c join fetch tasks t on t.id=c.task_id",
            nativeQuery = true)  // native query
    List<Comment> findAllCustom();
}
