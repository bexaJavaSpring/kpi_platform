package uz.java.kpisystem.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.java.kpisystem.dto.ApiResponse;
import uz.java.kpisystem.dto.checkListItems.CheckListItemsFilter;
import uz.java.kpisystem.dto.checkListItems.CheckListItemsRequest;
import uz.java.kpisystem.dto.checkListItems.CheckListItemsResponse;
import uz.java.kpisystem.entity.CheckListItem;
import uz.java.kpisystem.event.CheckListItemsCacheEvictEvent;
import uz.java.kpisystem.exception.CustomNotFoundException;
import uz.java.kpisystem.exception.RedisNotSerializableException;
import uz.java.kpisystem.listener.CacheEvictEventListener;
import uz.java.kpisystem.mapper.CheckListItemsMapper;
import uz.java.kpisystem.repository.CheckListItemRepository;
import uz.java.kpisystem.repository.CheckListRepository;
import uz.java.kpisystem.repository.UserRepository;
import uz.java.kpisystem.specifications.CheckListItemsSpecification;

import uz.java.kpisystem.specifications.SearchSpecification;
import org.springframework.data.domain.Pageable;
import uz.java.kpisystem.util.CachePrefix;

import java.util.List;


@Service
@RequiredArgsConstructor
public class CheckListItemsService implements ICheckListItemsService {

    private final CheckListItemRepository repository;
    private final CheckListRepository checkListRepository;
    private final UserRepository userRepository;
    private final CheckListItemsMapper mapper;
    private  final CacheManagerService cacheManagerService;
    private final CacheEvictEventListener cacheEvictEventListener;

    private final String msgcode = "checkListItem.not.found";

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<CheckListItemsResponse>> getAll(CheckListItemsFilter filter) {
        Object data = cacheManagerService.get(String.valueOf(filter.hashCode()), CachePrefix.CHECKLIST_ITEMS);
        if(data != null) {
            return (ApiResponse<List<CheckListItemsResponse>>) data;
        }

        CheckListItemsSpecification specification = new CheckListItemsSpecification(filter);
        Pageable pagination = SearchSpecification.getPageable(filter.getPage(),filter.getLimit(),filter.getSortBy());
        List<CheckListItemsResponse>  responses = repository.findAll(specification,pagination).stream().map(mapper::toResponse).toList();
        ApiResponse<List<CheckListItemsResponse>> apiResponse = new ApiResponse<>(responses);
        cacheManagerService.put(String.valueOf(filter.hashCode()), CachePrefix.CHECKLIST_ITEMS, apiResponse);
        return apiResponse;
    }

    @Override
    @Transactional
    public Long create(CheckListItemsRequest request) {
        CheckListItem item = mapper.toEntity(request);
        item.setCheckList(
                checkListRepository.findById(request.getCheckListId())
                        .orElseThrow(() -> new CustomNotFoundException("checklist.not.found"))
        );
        if (request.getUserId() != null) {
            item.setUser(
                    userRepository.findById(request.getUserId())
                            .orElseThrow(() -> new CustomNotFoundException("user.not.found"))
            );
        }
        cacheEvictEventListener.handleCacheEvict(new CheckListItemsCacheEvictEvent(CachePrefix.CHECKLIST_ITEMS));
        return repository.save(item).getId();
    }

    @Override
    @Transactional
    public CheckListItemsResponse update(Long id, CheckListItemsRequest request) {
        CheckListItem item = repository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException(msgcode));
        mapper.updateFromRequest(request, item);
        item.setCheckList(
                checkListRepository.findById(request.getCheckListId())
                        .orElseThrow(() -> new CustomNotFoundException("checklist.not.found"))
        );
        if (request.getUserId() != null) {
            item.setUser(
                    userRepository.findById(request.getUserId())
                            .orElseThrow(() -> new CustomNotFoundException("user.not.found"))
            );
        }
        cacheEvictEventListener.handleCacheEvict(new CheckListItemsCacheEvictEvent(CachePrefix.CHECKLIST_ITEMS));
        return mapper.toResponse(repository.save(item));
    }

    @Override
    @Transactional(readOnly = true)
    public CheckListItemsResponse getOne(Long id) {
        Object data = cacheManagerService.get(id.toString(),CachePrefix.CHECKLIST_ITEMS);
        if(data != null) {
            return (CheckListItemsResponse) data;
        }
        CheckListItemsResponse response = mapper.toResponse(
                repository.findById(id).orElseThrow(() -> new CustomNotFoundException(msgcode))
        );
        try{
            cacheManagerService.put(id.toString(),CachePrefix.CHECKLIST_ITEMS,response);
        }catch (Exception e) {
            throw new RedisNotSerializableException(e.getMessage());
        }
        return response;
    }

    @Override
    @Transactional
    public Boolean delete(Long id) {
        CheckListItem item = repository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException(msgcode));
        item.makeAsDeleted();
        repository.save(item);
        cacheEvictEventListener.handleCacheEvict(new CheckListItemsCacheEvictEvent(CachePrefix.CHECKLIST_ITEMS));
        return true;
    }
}