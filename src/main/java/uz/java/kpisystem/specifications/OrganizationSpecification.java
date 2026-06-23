package uz.java.kpisystem.specifications;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;
import uz.java.kpisystem.dto.organization.OrganizationFilter;
import uz.java.kpisystem.entity.Organization;

import java.util.ArrayList;
import java.util.List;

public record OrganizationSpecification(OrganizationFilter filter) implements Specification<Organization> {

    @Override
    public Predicate toPredicate(Root<Organization> root,
                                 CriteriaQuery<?> query,
                                 CriteriaBuilder criteriaBuilder) {

        List<Predicate> predicates = new ArrayList<>();

        if (filter.getName() != null && !filter.getName().isBlank()) {
            predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                    "%" + filter.getName().toLowerCase() + "%"
            ));
        }

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }
}