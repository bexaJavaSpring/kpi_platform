package uz.java.kpisystem.dto.comment;

import lombok.Data;
import uz.java.kpisystem.dto.BaseFilter;

@Data
public class CommentFilter extends BaseFilter {

    private String text;

    private Long taskId;

    public CommentFilter(Integer page, Integer limit, String sortBy, String text, Long taskId) {
        super(page, limit, sortBy);
        this.text = text;
        this.taskId = taskId;
    }
}
