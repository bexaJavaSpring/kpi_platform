package uz.java.kpisystem.specifications;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import uz.java.kpisystem.dto.task.TaskFilter;
import uz.java.kpisystem.entity.Task;

import java.util.ArrayList;
import java.util.List;

public record TaskSpecification(TaskFilter filter) implements Specification<Task> {
    @Override
    public Predicate toPredicate(Root<Task> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

//        predicates.add(criteriaBuilder.equal(root.get("deleted"), false));

        if (filter.getName() != null)
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),
                    "%" + filter.getName().toLowerCase() + "%"));

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
