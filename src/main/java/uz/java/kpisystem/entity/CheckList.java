package uz.java.kpisystem.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "checklist")
public class CheckList extends Auditable {
    private String name;

    @OneToMany(mappedBy = "checkList", fetch = FetchType.LAZY)
    private Set<CheckListItem> items = new HashSet<>();
}
