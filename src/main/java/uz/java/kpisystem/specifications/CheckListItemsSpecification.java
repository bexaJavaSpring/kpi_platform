package uz.java.kpisystem.specifications;


import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import uz.java.kpisystem.dto.checkListItems.CheckListItemsFilter;
import uz.java.kpisystem.entity.CheckListItem;

import java.util.ArrayList;
import java.util.List;

public record CheckListItemsSpecification(CheckListItemsFilter filter) implements Specification<CheckListItem> {

    @Override
    public Predicate toPredicate(Root<CheckListItem> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();
        if (filter.getName() != null)
            predicates.add(cb.like(cb.lower(root.get("name")), "%" + filter.getName().toLowerCase() + "%"));
        if (filter.getCheckListId() != null)
            predicates.add(cb.equal(root.get("checkList").get("id"), filter.getCheckListId()));
        return cb.and(predicates.toArray(new Predicate[0]));
    }
}
