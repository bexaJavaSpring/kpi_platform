package uz.java.kpisystem.dto.checkList;

import lombok.Getter;
import lombok.Setter;
import uz.java.kpisystem.dto.BaseFilter;

@Getter
@Setter

public class CheckListFilter extends BaseFilter{
    private String name;

    public CheckListFilter(Integer page, Integer limit, String sortBy,String name) {
        super(page, limit, sortBy);
        this.name = name;
    }
}
