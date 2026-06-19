package uz.java.kpisystem.service;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.java.kpisystem.dto.checkList.CheckListFilter;
import uz.java.kpisystem.dto.checkList.CheckListRequest;
import uz.java.kpisystem.dto.checkList.CheckListResponse;
import uz.java.kpisystem.entity.CheckList;
import uz.java.kpisystem.entity.CheckListItem;
import uz.java.kpisystem.exception.CustomNotFoundException;
import uz.java.kpisystem.mapper.CheckListMapper;
import uz.java.kpisystem.repository.CheckListItemRepository;
import uz.java.kpisystem.repository.CheckListRepository;
import uz.java.kpisystem.specifications.CheckListSpecification;
import uz.java.kpisystem.specifications.SearchSpecification;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CheckListService implements ICheckListService {

    private final CheckListRepository repository;
    private final CheckListItemRepository checkListItemRepository;
    private final CheckListMapper mapper;
    private final String msgcode = "checklist.not.found";

    @Override
    @Transactional(readOnly = true)
    public List<CheckListResponse> getAll(CheckListFilter filter) {
        Pageable pagination = SearchSpecification.getPageable(filter.getPage(), filter.getLimit(), filter.getSortBy());
        return repository.findAll(new CheckListSpecification(filter), pagination)
                .stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional
    public Long create(CheckListRequest request) {
        Set<CheckListItem> items = new HashSet<>();
        CheckList checkList = mapper.toEntity(request);
        if (request.getCheckListItems() != null && !request.getCheckListItems().isEmpty()) {
            request.getCheckListItems().forEach(id -> {
                CheckListItem checkListItem = checkListItemRepository.findById(id).orElseThrow(
                        () -> new CustomNotFoundException(msgcode)
                );
                items.add(checkListItem);
            });
            checkList.setItems(items);
        }
        return repository.save(checkList).getId();
    }

    @Override
    @Transactional
    public CheckListResponse update(Long id, CheckListRequest request) {
        Set<CheckListItem> items = new HashSet<>();
        CheckList checkList = repository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException(msgcode));
        mapper.updateFromRequest(request, checkList);
        if (request.getCheckListItems() != null) {
            request.getCheckListItems().forEach(itemId -> {
                CheckListItem checkListItem = checkListItemRepository.findById(itemId).orElseThrow(
                        () -> new CustomNotFoundException(msgcode)
                );
                items.add(checkListItem);
            });
            checkList.setItems(items);
        }
        return mapper.toResponse(repository.save(checkList));
    }

    @Override
    @Transactional(readOnly = true)
    public CheckListResponse getOne(Long id) {
        return mapper.toResponse(
                repository.findById(id).orElseThrow(() -> new CustomNotFoundException(msgcode))
        );
    }

    @Override
    @Transactional
    public Boolean delete(Long id) {
        CheckList checkList = repository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException(msgcode));
        checkList.makeAsDeleted();
        repository.save(checkList);
        return true;
    }
}