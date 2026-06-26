package uz.java.kpisystem.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.java.kpisystem.dto.ApiResponse;
import uz.java.kpisystem.dto.file.FileResponse;
import uz.java.kpisystem.dto.file.FileStat;
import uz.java.kpisystem.dto.task.TaskFilter;
import uz.java.kpisystem.dto.task.TaskRequest;
import uz.java.kpisystem.dto.task.TaskResponse;
import uz.java.kpisystem.dto.taskTag.TaskTagResponse;
import uz.java.kpisystem.entity.*;
import uz.java.kpisystem.event.GenericCacheEvictEvent;
import uz.java.kpisystem.exception.CustomNotFoundException;
import uz.java.kpisystem.exception.GenericRuntimeException;
import uz.java.kpisystem.exception.RedisNotSerializableException;
import uz.java.kpisystem.listener.CacheEvictEventListener;
import uz.java.kpisystem.mapper.TaskMapper;
import uz.java.kpisystem.repository.*;
import uz.java.kpisystem.specifications.SearchSpecification;
import uz.java.kpisystem.specifications.TaskSpecification;
import uz.java.kpisystem.util.CachePrefix;

import java.util.ArrayList;
import java.util.List;

@Slf4j
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
    private  final FileService fileService;
    private final ProjectRepository projectRepository;


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
        cacheEvictEventListener.handleCacheEvict(new GenericCacheEvictEvent<TaskService>(CachePrefix.TASK));
        return task.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<TaskResponse>> getAll(TaskFilter filter) {
        String cacheKey = cacheKey(filter);
        Object data = cacheManagerService.get(cacheKey, CachePrefix.TASK);
        if (data != null) {
            return (ApiResponse<List<TaskResponse>>) data;
        }
        TaskSpecification spec = new TaskSpecification(filter);
        Pageable pagination = SearchSpecification.getPageable(filter.getPage(), filter.getLimit(),
                filter.getSortBy());

        List<TaskResponse> all = repository.findAll(spec, pagination).stream()
                .map(this::toTaskResponse).toList();

        ApiResponse<List<TaskResponse>> apiResponse = new ApiResponse<>(all);
        try {
            cacheManagerService.put(cacheKey, CachePrefix.TASK, apiResponse);
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
        TaskResponse response = toTaskResponse(task);
        ApiResponse<TaskResponse> apiResponse = new ApiResponse<>(response);
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
        if (request.getParentId() != null)
            validateNoCycle(id, request.getParentId());
        mapper.updateFromRequest(request, task);
        repository.save(task);
        cacheEvictEventListener.handleCacheEvict(new GenericCacheEvictEvent<TaskService>(CachePrefix.TASK));
        return new ApiResponse<>(toTaskResponse(task));
    }

    @Override
    @Transactional
    public Boolean delete(Long id) {
        Task task = repository.findById(id).orElseThrow(() -> new CustomNotFoundException("task.not.found"));
        task.makeAsDeleted();
        repository.save(task);
        cacheEvictEventListener.handleCacheEvict(new GenericCacheEvictEvent<TaskService>(CachePrefix.TASK));
        return true;
    }

    @Override
    @Transactional
    public Long copy(Long id) {
        Task task = repository.findById(id).orElseThrow(() -> new CustomNotFoundException("task.not.found"));
        if (Boolean.TRUE.equals(task.getDeleted())) throw new CustomNotFoundException("task.not.found");
        Task copiedTask = new Task();
        mapper.copyFromExisting(task,copiedTask);
        copiedTask.setDeleted(false);
        copiedTask.setParentId(task.getParentId());
        repository.save(copiedTask);


        taskMemberRepository.findAllByTask(task).forEach( member -> {
            TaskMember newMember = new TaskMember();
            newMember.setTask(copiedTask);
            newMember.setUser(member.getUser());
            taskMemberRepository.save(newMember);
        });

        taskTagRepository.findAllByTask(task).forEach(tag -> {
            TaskTag newTag = new TaskTag();
            newTag.setTask(copiedTask);
            newTag.setName(tag.getName());
            taskTagRepository.save(newTag);
        });

        cacheEvictEventListener.handleCacheEvict(new GenericCacheEvictEvent<TaskService>(CachePrefix.TASK));
        return copiedTask.getId();
    }

    @Override
    @Transactional
    public Boolean moveToAnotherProject(Long taskId, Long projectId) {
        Task task = repository.findById(taskId).orElseThrow(() -> new CustomNotFoundException("task.not.found"));
        if (Boolean.TRUE.equals(task.getDeleted())) throw new CustomNotFoundException("task.not.found");
        Project project = projectRepository.findById(projectId).orElseThrow(() -> new CustomNotFoundException("project.not.found"));
        if (Boolean.TRUE.equals(project.getDeleted())) throw new CustomNotFoundException("project.not.found");
        task.setProject(project);
        repository.save(task);
        cacheEvictEventListener.handleCacheEvict(new GenericCacheEvictEvent<TaskService>(CachePrefix.TASK));
        return true;
    }

    private TaskResponse toTaskResponse(Task task) {
        TaskResponse response = mapper.toResponse(task);
        response.setAttachmentUrls(toFileResponses(task.getAttachmentUrls()));
        response.setChildren(buildChildren(task.getId()));
        response.setTags(this.getTags(task.getId()));
        return response;
    }

    private List<TaskTagResponse> getTags(Long taskId) {
        return taskTagRepository.findAllByTaskId(taskId).stream()
                .map(tag -> {
                    TaskTagResponse dto = new TaskTagResponse();
                    dto.setId(tag.getId());
                    dto.setName(tag.getName());
                    return dto;
                })
                .toList();
    };


    private String cacheKey(TaskFilter filter) {
        return filter.getPage() + "_" + filter.getLimit() + "_"
                + filter.getSortBy() + "_" + filter.getName();
    }

    // newParentId'dan yuqoriga (ajdodlar bo'yicha) yurib, taskId'ga duch kelsa cikl bor
    private void validateNoCycle(Long taskId, Long newParentId) {
        Long ancestorId = newParentId;
        while (ancestorId != null) {
            if (ancestorId.equals(taskId))
                throw new GenericRuntimeException("task.parent.cycle");
            ancestorId = repository.findById(ancestorId)
                    .map(Task::getParentId)
                    .orElse(null);
        }
    }

    // parentId bo'yicha bolalarni topib, rekursiv ravishda daraxt quradi
    private List<TaskResponse> buildChildren(Long parentId) {
        return repository.findByParentIdAndDeletedFalse(parentId).stream()
                .map(this::toTaskResponse)
                .toList();
    }

    private List<FileResponse> toFileResponses(List<String> keys) {
        if (keys == null || keys.isEmpty()) return List.of();
        List<FileResponse> result = new ArrayList<>();
        for (String key : keys) {
            FileStat stat = fileService.stat(key);
            if (stat == null) continue;                 // Minioda yo'q -> tashlab ketamiz
            FileResponse fr = new FileResponse();
            fr.setFileName(key);
            fr.setSize(stat.size());
            fr.setContentType(stat.contentType());
            fr.setFileUrl(fileService.getPresignedUrl(key));
            result.add(fr);
        }
        return result;
    }

}
