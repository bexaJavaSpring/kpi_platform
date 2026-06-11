package uz.java.kpisystem.dto.checkListItem;

import lombok.Data;

import java.util.Set;

@Data
public class CheckListItemRequest {
    private String name;
    private Set<CheckListItemRequest> checkListItemList;
}
