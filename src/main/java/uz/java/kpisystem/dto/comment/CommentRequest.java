package uz.java.kpisystem.dto.comment;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CommentRequest {

    private String text;

    private Long taskId;

    private Long parentId;
}
