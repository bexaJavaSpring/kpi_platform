package uz.java.kpisystem.service;

import uz.java.kpisystem.dto.ApiResponse;
import uz.java.kpisystem.dto.task.TaskFilter;
import uz.java.kpisystem.dto.task.TaskRequest;
import uz.java.kpisystem.dto.task.TaskResponse;

import java.util.List;

public interface ITaskService {

    Long create(TaskRequest request);

    ApiResponse<List<TaskResponse>> getAll(TaskFilter filter);

    ApiResponse<TaskResponse> getOne(Long id);

    ApiResponse<TaskResponse> update(Long id, TaskRequest request);

    Boolean delete(Long id);

    Long copy(Long id);

    Boolean moveToAnotherProject(Long taskId, Long projectId);
}
