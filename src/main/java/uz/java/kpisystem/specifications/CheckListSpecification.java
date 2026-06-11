package uz.java.kpisystem.specifications;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import uz.java.kpisystem.dto.checkList.CheckListFilter;
import uz.java.kpisystem.entity.CheckList;

import java.util.ArrayList;
import java.util.List;

public record CheckListSpecification(CheckListFilter filter)  implements Specification<CheckList> {
    @Override
    public Predicate toPredicate(Root<CheckList> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        if (filter.getName() != null)
            predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("name")),
                    "%" + filter.getName().toLowerCase() + "%"));
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}
