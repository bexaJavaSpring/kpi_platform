package uz.java.kpisystem.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.java.kpisystem.dto.ApiResponse;
import uz.java.kpisystem.dto.task.TaskFilter;
import uz.java.kpisystem.dto.task.TaskRequest;
import uz.java.kpisystem.dto.task.TaskResponse;
import uz.java.kpisystem.entity.Task;
import uz.java.kpisystem.entity.TaskMember;
import uz.java.kpisystem.entity.TaskTag;
import uz.java.kpisystem.entity.User;
import uz.java.kpisystem.event.ProjectCacheEvictEvent;
import uz.java.kpisystem.exception.CustomNotFoundException;
import uz.java.kpisystem.exception.RedisNotSerializableException;
import uz.java.kpisystem.listener.CacheEvictEventListener;
import uz.java.kpisystem.mapper.TaskMapper;
import uz.java.kpisystem.repository.TaskMemberRepository;
import uz.java.kpisystem.repository.TaskRepository;
import uz.java.kpisystem.repository.TaskTagRepository;
import uz.java.kpisystem.repository.UserRepository;
import uz.java.kpisystem.specifications.SearchSpecification;
import uz.java.kpisystem.specifications.TaskSpecification;
import uz.java.kpisystem.util.CachePrefix;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TaskService implements ITaskService {

    private final TaskMapper mapper;
    private final TaskRepository repository;
    private final UserRepository userRepository;
    private final TaskMemberRepository taskMemberRepository;
    private final TaskTagRepository taskTagRepository;
    private final CacheManagerService cacheManagerService;
    private final CacheEvictEventListener cacheEvictEventListener;

    @Override
    @Transactional
    public Long create(TaskRequest request) {
        Task task = mapper.toEntity(request);
        if (!request.getAssignerIds().isEmpty()) {
            request.getAssignerIds().stream().forEach(assignerId -> {
                User user = userRepository.findById(assignerId).orElseThrow(
                        () -> new CustomNotFoundException("member.not.found")
                );
                TaskMember member = new TaskMember();
                member.setUser(user);
                member.setTask(task);
                taskMemberRepository.save(member);
            });
        }
        if (!request.getTagIds().isEmpty()) {
            request.getTagIds().stream().forEach(tagId -> {
                TaskTag taskTag = taskTagRepository.findById(tagId).orElseThrow(
                        () -> new CustomNotFoundException("task.tag.not.found")
                );
                taskTag.setTask(task);
                taskTagRepository.save(taskTag);
            });
        }
        if (request.getParentId() != null) {
            task.setParentId(request.getParentId());
        }
        task.setDeleted(false);
        repository.save(task);
        cacheEvictEventListener.handleCacheEvict(new ProjectCacheEvictEvent(CachePrefix.TASK));
        return task.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<TaskResponse>> getAll(TaskFilter filter) {
        Object data = cacheManagerService.get(String.valueOf(filter.hashCode()), CachePrefix.TASK);
        if (data != null) {
            return (ApiResponse<List<TaskResponse>>) data;
        }
        TaskSpecification spec = new TaskSpecification(filter);
        Pageable pagination = SearchSpecification.getPageable(filter.getPage(), filter.getLimit(),
                filter.getSortBy());

        List<TaskResponse> all = repository.findAll(spec, pagination).stream()
                .map(mapper::toResponse).toList();
        ApiResponse<List<TaskResponse>> apiResponse = new ApiResponse<>(all);
        try {
            cacheManagerService.put(String.valueOf(filter.hashCode()), CachePrefix.TASK, apiResponse);
        } catch (Exception e) {
            throw new RedisNotSerializableException(e.getMessage());
        }
        return apiResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<TaskResponse> getOne(Long id) {
        Object data = cacheManagerService.get(id.toString(), CachePrefix.TASK);
        if (data != null) return (ApiResponse<TaskResponse>) data;

        Task task = repository.findById(id).orElseThrow(() -> new CustomNotFoundException("task.not.found"));
        if (Boolean.TRUE.equals(task.getDeleted())) throw new CustomNotFoundException("task.not.found");
        ApiResponse<TaskResponse> apiResponse = new ApiResponse<>(mapper.toResponse(task));
        try {
            cacheManagerService.put(id.toString(), CachePrefix.TASK, apiResponse);
        } catch (Exception e) {
            throw new RedisNotSerializableException(e.getMessage());
        }
        return apiResponse;
    }

    @Override
    @Transactional
    public ApiResponse<TaskResponse> update(Long id, TaskRequest request) {
        Task task = repository.findById(id).orElseThrow(() -> new CustomNotFoundException("task.not.found"));
        if (Boolean.TRUE.equals(task.getDeleted())) throw new CustomNotFoundException("task.not.found");
        mapper.updateFromRequest(request, task);
        repository.save(task);
        cacheEvictEventListener.handleCacheEvict(new ProjectCacheEvictEvent(CachePrefix.TASK));
        return new ApiResponse<>(mapper.toResponse(task));
    }

    @Override
    @Transactional
    public Boolean delete(Long id) {
        Task task = repository.findById(id).orElseThrow(() -> new CustomNotFoundException("task.not.found"));
        task.makeAsDeleted();
        repository.save(task);
        cacheEvictEventListener.handleCacheEvict(new ProjectCacheEvictEvent(CachePrefix.TASK));
        return true;
    }
}
