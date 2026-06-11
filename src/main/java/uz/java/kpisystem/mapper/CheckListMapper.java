package uz.java.kpisystem.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import uz.java.kpisystem.dto.checkList.CheckListRequest;
import uz.java.kpisystem.dto.checkList.CheckListResponse;
import uz.java.kpisystem.entity.CheckList;

@Mapper(componentModel = "spring")
public interface CheckListMapper {

    CheckListResponse toResponse(CheckList checkList);

    CheckList toEntity(CheckListRequest request);

    void updateFromRequest(CheckListRequest request, @MappingTarget CheckList checkList);
}
