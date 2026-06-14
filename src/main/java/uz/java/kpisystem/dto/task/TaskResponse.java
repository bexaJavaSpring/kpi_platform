package uz.java.kpisystem.dto.task;

import lombok.Data;

@Data
public class TaskResponse {
    private Long id;
    private String name;
    private String description;
    private Long parentId;
}
