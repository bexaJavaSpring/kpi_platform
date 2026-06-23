package uz.java.kpisystem.service;

import uz.java.kpisystem.dto.ApiResponse;
import uz.java.kpisystem.dto.group.GroupFilter;
import uz.java.kpisystem.dto.group.GroupRequest;
import uz.java.kpisystem.dto.group.GroupResponse;
import uz.java.kpisystem.entity.Group;

import java.util.List;

public interface IGroupService {

    ApiResponse<List<GroupResponse>> getAll(GroupFilter groupFilter);

    Long create(GroupRequest body);

    Long update(Long id,GroupRequest body);

    GroupResponse getOne(Long id);

    Boolean delete(Long id);
}
