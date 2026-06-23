package uz.java.kpisystem.dto.group;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import uz.java.kpisystem.dto.BaseFilter;

@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class GroupFilter extends BaseFilter {
    private String name;
    private Integer taskCount;

    public GroupFilter(Integer page, Integer limit, String sortBy, String name, Integer taskCount) {
        super(page, limit, sortBy);
        this.name = name;
        this.taskCount = taskCount;
    }
}
