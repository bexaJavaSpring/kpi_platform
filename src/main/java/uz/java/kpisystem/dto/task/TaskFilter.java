package uz.java.kpisystem.dto.task;

import lombok.Data;
import uz.java.kpisystem.dto.BaseFilter;

@Data
public class TaskFilter extends BaseFilter {
    private String name;

    public TaskFilter(Integer page, Integer limit, String sortBy, String name) {
        super(page, limit, sortBy);
        this.name = name;
    }
}
