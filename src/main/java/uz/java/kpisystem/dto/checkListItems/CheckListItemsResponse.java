package uz.java.kpisystem.dto.checkListItems;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CheckListItemsResponse {
    private Long id;
    private String name;
    private Long checkListId;
    private String checkListName;
    private Long userId;
    private String userFirstName;
    private String userLastName;
}
