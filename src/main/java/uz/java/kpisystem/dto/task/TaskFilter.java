package uz.java.kpisystem.dto.task;

import lombok.Data;
import lombok.EqualsAndHashCode;
import uz.java.kpisystem.dto.BaseFilter;

@Data
@EqualsAndHashCode(callSuper = true)
public class TaskFilter extends BaseFilter {
    private String name;

    public TaskFilter(Integer page, Integer limit, String sortBy, String name) {
        super(page, limit, sortBy);
        this.name = name;
    }
}
