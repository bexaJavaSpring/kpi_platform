package uz.java.kpisystem.service;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.java.kpisystem.dto.ApiResponse;
import uz.java.kpisystem.dto.group.GroupFilter;
import uz.java.kpisystem.dto.group.GroupRequest;
import uz.java.kpisystem.dto.group.GroupResponse;
import uz.java.kpisystem.entity.Group;
import uz.java.kpisystem.event.GroupCacheEvictEvent;
import uz.java.kpisystem.exception.CustomNotFoundException;
import uz.java.kpisystem.exception.RedisNotSerializableException;
import uz.java.kpisystem.listener.CacheEvictEventListener;
import uz.java.kpisystem.mapper.GroupMapper;
import uz.java.kpisystem.repository.GroupRepository;
import uz.java.kpisystem.specifications.GroupSpecification;
import uz.java.kpisystem.specifications.SearchSpecification;
import uz.java.kpisystem.util.CachePrefix;

import java.util.List;
import java.util.Optional;

@Service
public class GroupService implements IGroupService {
    private final GroupRepository repository;
    private final GroupMapper mapper;
    private final CacheManagerService cacheManagerService;
    private final CacheEvictEventListener cacheEvictEventListener;
    private final String msgcode = "group.not.found";

    public GroupService(GroupRepository repository, GroupMapper mapper, CacheManagerService cacheManagerService, CacheEvictEventListener cacheEvictEventListener) {
        this.repository = repository;
        this.mapper = mapper;
        this.cacheManagerService = cacheManagerService;
        this.cacheEvictEventListener = cacheEvictEventListener;
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<GroupResponse>> getAll(GroupFilter groupFilter) {
        Object data = cacheManagerService.get(String.valueOf(groupFilter.hashCode()), CachePrefix.GROUP);
        if (data != null)
            return (ApiResponse<List<GroupResponse>>) data;

        GroupSpecification spec = new GroupSpecification(groupFilter);
        Pageable pagination = SearchSpecification.getPageable(groupFilter.getPage(), groupFilter.getLimit(),
                groupFilter.getSortBy());
        List<GroupResponse> response = repository.findAll(spec, pagination).stream().map(mapper::toResponse).toList();
        ApiResponse<List<GroupResponse>> groupResponse = new ApiResponse<>(response);
        cacheManagerService.put(String.valueOf(groupFilter.hashCode()), CachePrefix.GROUP, groupResponse);
        return groupResponse;
    }

    @Override
    @Transactional
    public Long create(GroupRequest body) {
        Group build = Group.builder().name(body.getName()).taskCount(body.getTaskCount()).build();
        Group save = repository.save(build);
        return save.getId();
    }


    @Override
    @Transactional
    public Long update(Long id, GroupRequest body) {
        Optional<Group> opt = repository.findById(id);
        if (!opt.isPresent())
            throw new CustomNotFoundException(msgcode);
        Group group = opt.get();
        group.setName(body.getName());
        group.setTaskCount(body.getTaskCount());
        Group save = repository.save(group);
        cacheEvictEventListener.handleCacheEvict(new GroupCacheEvictEvent(CachePrefix.GROUP));
        return save.getId();
    }

    @Override
    @Transactional(readOnly = true)
    public GroupResponse getOne(Long id) {
        Object data = cacheManagerService.get(id.toString(), CachePrefix.GROUP);
        if (data != null)
            return (GroupResponse) data;

        Optional<Group> opt = repository.findById(id);
        if (!opt.isPresent())
            throw new CustomNotFoundException(msgcode);

        Group group = opt.get();
        GroupResponse response = mapper.toResponse(group);
        try {
            cacheManagerService.put(id.toString(), CachePrefix.GROUP, response);
        } catch (Exception e) {
            throw new RedisNotSerializableException(e.getMessage());
        }
        return response;
    }

    @Override
    @Transactional
    public Boolean delete(Long id) {
        Group group = repository.findById(id).orElseThrow(
                () -> new CustomNotFoundException(msgcode)
        );
        group.makeAsDeleted(); // soft delete: bazadan o'chirmaymiz, faqat deleted=true qilamiz
        repository.save(group);
        cacheEvictEventListener.handleCacheEvict(new GroupCacheEvictEvent(CachePrefix.GROUP));
        return true;
    }
}
