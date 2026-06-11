package uz.java.kpisystem.dto.checkListItems;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import uz.java.kpisystem.dto.BaseFilter;

@Getter
@Setter
@Data
public class CheckListItemsFilter extends BaseFilter{
    private String name;
    private Long checkListId;


    public CheckListItemsFilter(Integer page, Integer limit, String sortBy, String name, Long checkListId) {
        super(page, limit, sortBy);
        this.name = name;
        this.checkListId = checkListId;
    }
}
