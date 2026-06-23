package uz.java.kpisystem.dto.group;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class GroupResponse implements Serializable {
    private Long id;
    private String name;
    private Integer taskCount;
}
