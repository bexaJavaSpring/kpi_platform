package uz.java.kpisystem.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import uz.java.kpisystem.entity.CheckListItem;

public interface CheckListItemRepository extends JpaRepository<CheckListItem,Long>, JpaSpecificationExecutor<CheckListItem> {
}
