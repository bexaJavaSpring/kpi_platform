package uz.java.kpisystem.dto.checkList;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
public class CheckListRequest {
    @NotBlank(message = "checkList.name.must.not.be.blank")
    private String name;
    Set<Long> checkList =  new HashSet<>();
}
