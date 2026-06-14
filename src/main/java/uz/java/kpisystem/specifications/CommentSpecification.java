package uz.java.kpisystem.specifications;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import uz.java.kpisystem.dto.comment.CommentFilter;
import uz.java.kpisystem.entity.Comment;

import java.util.ArrayList;
import java.util.List;

public record CommentSpecification(CommentFilter filter) implements Specification<Comment> {
    @Override
    public Predicate toPredicate(Root<Comment> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        predicates.add(criteriaBuilder.or(criteriaBuilder.equal(root.get("deleted"), false)));

        if (filter.getTaskId() != null)
            predicates.add(criteriaBuilder.equal(root.get("task").get("id"), filter.getTaskId()));

        if (filter.getText() != null)
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("text")),
                    "%" + filter.getText().toLowerCase() + "%"));

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
