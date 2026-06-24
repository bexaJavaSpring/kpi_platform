package uz.java.kpisystem.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import uz.java.kpisystem.dto.checkListItems.CheckListItemsRequest;
import uz.java.kpisystem.dto.checkListItems.CheckListItemsResponse;
import uz.java.kpisystem.entity.CheckListItem;

@Mapper(componentModel = "spring")
public interface CheckListItemsMapper {

    @Mapping(target = "checkListId", source = "checkList.id")
    @Mapping(target = "checkListName", source = "checkList.name")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "userFirstName", source = "user.firstName")
    @Mapping(target = "userLastName", source = "user.lastName")
    CheckListItemsResponse toResponse(CheckListItem entity);

    @Mapping(target = "checkList", ignore = true)
    @Mapping(target = "user", ignore = true)
    CheckListItem toEntity(CheckListItemsRequest request);

    @Mapping(target = "checkList", ignore = true)
    @Mapping(target = "user", ignore = true)
    void updateFromRequest(CheckListItemsRequest request, @MappingTarget CheckListItem entity);
}