package uz.java.kpisystem.dto.checkListItems;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class CheckListItemsRequest {
    @NotBlank(message = "checkListItem.name.must.not.be.blank")
    private String name;
    @NotNull(message = "checkListItem.checkListId.must.not.be.null")
    private Long checkListId;
    private Long userId;
}
