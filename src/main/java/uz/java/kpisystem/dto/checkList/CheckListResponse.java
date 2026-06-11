package uz.java.kpisystem.dto.checkList;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import uz.java.kpisystem.dto.checkListItem.CheckListItemResponse;
import java.util.Set;

@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class CheckListResponse {
    private String name;
    private Set<CheckListItemResponse> checkListItemList;
}
