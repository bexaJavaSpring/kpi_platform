package uz.java.kpisystem.dto.comment;

import lombok.Data;

@Data
public class CommentResponse {

    private String text;

    private Long parentId;
}
