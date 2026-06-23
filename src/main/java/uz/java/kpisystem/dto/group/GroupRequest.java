package uz.java.kpisystem.dto.group;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GroupRequest {
    @NotBlank(message = "group.name.must.not.be.blank")
    private String name;
    private Integer taskCount;
}
