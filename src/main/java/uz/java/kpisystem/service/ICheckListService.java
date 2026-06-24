package uz.java.kpisystem.service;

import uz.java.kpisystem.dto.ApiResponse;
import uz.java.kpisystem.dto.checkList.CheckListFilter;
import uz.java.kpisystem.dto.checkList.CheckListRequest;
import uz.java.kpisystem.dto.checkList.CheckListResponse;

import java.util.List;

public interface ICheckListService {
    ApiResponse<List<CheckListResponse>>  getAll(CheckListFilter checkListFilter);

    Long create(CheckListRequest request);

    CheckListResponse update(Long id,CheckListRequest request);

    CheckListResponse getOne(Long id);

    Boolean delete(Long id);

}
