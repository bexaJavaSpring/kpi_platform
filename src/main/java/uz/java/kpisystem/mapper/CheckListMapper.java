package uz.java.kpisystem.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import uz.java.kpisystem.dto.checkList.CheckListRequest;
import uz.java.kpisystem.dto.checkList.CheckListResponse;
import uz.java.kpisystem.entity.CheckList;

@Mapper(componentModel = "spring")
public interface CheckListMapper {

    @Mapping(target = "checkListItemsList", source = "items")
    CheckListResponse toResponse(CheckList checkList);

    @Mapping(target = "items", ignore = true)
    CheckList toEntity(CheckListRequest request);

    @Mapping(target = "items", ignore = true)
    void updateFromRequest(CheckListRequest request, @MappingTarget CheckList checkList);
}
