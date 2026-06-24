package uz.java.kpisystem.service;

import uz.java.kpisystem.dto.checkListItems.CheckListItemsFilter;
import uz.java.kpisystem.dto.checkListItems.CheckListItemsRequest;
import uz.java.kpisystem.dto.checkListItems.CheckListItemsResponse;

import java.util.List;

public interface ICheckListItemsService {

    List<CheckListItemsResponse> getAll(CheckListItemsFilter filter);

    Long create(CheckListItemsRequest request);

    CheckListItemsResponse update(Long id, CheckListItemsRequest request);

    CheckListItemsResponse getOne(Long id);

    Boolean delete(Long id);

}
