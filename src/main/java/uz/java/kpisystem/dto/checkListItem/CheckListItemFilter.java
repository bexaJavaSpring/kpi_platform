package uz.java.kpisystem.dto.checkListItem;

import lombok.Getter;
import lombok.Setter;
import uz.java.kpisystem.dto.BaseFilter;

@Getter
@Setter

public class CheckListItemFilter extends BaseFilter{
    private String name;

    public CheckListItemFilter(Integer page, Integer limit, String sortBy,String name) {
        super(page, limit, sortBy);
        this.name = name;
    }
}
