package uz.java.kpisystem.dto.checkList;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import uz.java.kpisystem.dto.checkListItems.CheckListItemsResponse;
import java.util.Set;

@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class CheckListResponse {
    private String name;
    private Set<CheckListItemsResponse> checkListItemsList;
}
