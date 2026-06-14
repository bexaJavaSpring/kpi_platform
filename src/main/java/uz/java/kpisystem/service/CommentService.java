package uz.java.kpisystem.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.java.kpisystem.dto.ApiResponse;
import uz.java.kpisystem.dto.comment.CommentFilter;
import uz.java.kpisystem.dto.comment.CommentRequest;
import uz.java.kpisystem.dto.comment.CommentResponse;
import uz.java.kpisystem.entity.Comment;
import uz.java.kpisystem.entity.Task;
import uz.java.kpisystem.event.ProjectCacheEvictEvent;
import uz.java.kpisystem.exception.CustomNotFoundException;
import uz.java.kpisystem.exception.RedisNotSerializableException;
import uz.java.kpisystem.listener.CacheEvictEventListener;
import uz.java.kpisystem.mapper.CommentMapper;
import uz.java.kpisystem.repository.CommentRepository;
import uz.java.kpisystem.repository.TaskRepository;
import uz.java.kpisystem.specifications.CommentSpecification;
import uz.java.kpisystem.specifications.SearchSpecification;
import uz.java.kpisystem.util.CachePrefix;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService implements ICommentService {

    private final CommentRepository repository;
    private final TaskRepository taskRepository;
    private final CommentMapper mapper;
    private final CacheManagerService cacheManagerService;
    private final CacheEvictEventListener cacheEvictEventListener;

    @Override
    @Transactional
    public Long create(CommentRequest request) {
        Comment comment = mapper.toEntity(request);
        Task task = taskRepository.findById(request.getTaskId())
                .orElseThrow(() -> new CustomNotFoundException("comment.not.found"));

        comment.setTask(task);
        comment.setDeleted(false);
        repository.save(comment);
        cacheEvictEventListener.handleCacheEvict(new ProjectCacheEvictEvent(CachePrefix.COMMENT));
        return comment.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<CommentResponse>> getAll(CommentFilter filter) {
        Object data = cacheManagerService.get(String.valueOf(filter.hashCode()), CachePrefix.COMMENT);
        if (data != null) {
            return (ApiResponse<List<CommentResponse>>) data;
        }
        CommentSpecification spec = new CommentSpecification(filter);
        Pageable pagination = SearchSpecification.getPageable(filter.getPage(), filter.getLimit(),
                filter.getSortBy());

        List<CommentResponse> all = repository.findAll(spec, pagination).stream().map(mapper::toResponse).toList();
        ApiResponse<List<CommentResponse>> apiResponse = new ApiResponse<>(all);
        try {
            cacheManagerService.put(String.valueOf(filter.hashCode()), CachePrefix.COMMENT, apiResponse);
        } catch (Exception e) {
            throw new RedisNotSerializableException(e.getMessage());
        }
        return apiResponse;
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<CommentResponse> getOne(Long id) {
        Object data = cacheManagerService.get(id.toString(), CachePrefix.COMMENT);
        if (data != null) return (ApiResponse<CommentResponse>) data;

        Comment comment = repository.findById(id).orElseThrow(() -> new CustomNotFoundException("comment.not.found"));
        if (comment.getDeleted()) throw new CustomNotFoundException("comment.not.found");
        try {
            cacheManagerService.put(id.toString(), CachePrefix.COMMENT, comment);
        } catch (Exception e) {
            throw new RedisNotSerializableException(e.getMessage());
        }
        return new ApiResponse<>(mapper.toResponse(comment));
    }

    @Override
    @Transactional
    public ApiResponse<CommentResponse> update(Long id, CommentRequest request) {
        Comment comment = repository.findById(id).orElseThrow(() -> new CustomNotFoundException("comment.not.found"));
        mapper.updateFromRequest(request, comment);
        repository.save(comment);
        cacheEvictEventListener.handleCacheEvict(new ProjectCacheEvictEvent(CachePrefix.COMMENT));
        return getOne(id);
    }

    @Override
    @Transactional
    public Boolean delete(Long id) {
        Comment comment = repository.findById(id).orElseThrow(() -> new CustomNotFoundException("comment.not.found"));
        comment.makeAsDeleted();
        repository.save(comment);
        cacheEvictEventListener.handleCacheEvict(new ProjectCacheEvictEvent(CachePrefix.COMMENT));
        return true;
    }
}
