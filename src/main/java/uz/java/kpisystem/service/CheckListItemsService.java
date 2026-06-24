package uz.java.kpisystem.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.java.kpisystem.dto.checkListItems.CheckListItemsFilter;
import uz.java.kpisystem.dto.checkListItems.CheckListItemsRequest;
import uz.java.kpisystem.dto.checkListItems.CheckListItemsResponse;
import uz.java.kpisystem.entity.CheckListItem;
import uz.java.kpisystem.exception.CustomNotFoundException;
import uz.java.kpisystem.mapper.CheckListItemsMapper;
import uz.java.kpisystem.repository.CheckListItemRepository;
import uz.java.kpisystem.repository.CheckListRepository;
import uz.java.kpisystem.repository.UserRepository;
import uz.java.kpisystem.specifications.CheckListItemsSpecification;
import uz.java.kpisystem.specifications.SearchSpecification;
import org.springframework.data.domain.Pageable;
import java.util.List;


@Service
@RequiredArgsConstructor
public class CheckListItemsService implements ICheckListItemsService {

    private final CheckListItemRepository repository;
    private final CheckListRepository checkListRepository;
    private final UserRepository userRepository;
    private final CheckListItemsMapper mapper;
    private final String msgcode = "checkListItem.not.found";

    @Override
    @Transactional(readOnly = true)
    public List<CheckListItemsResponse> getAll(CheckListItemsFilter filter) {
        Pageable pagination = SearchSpecification.getPageable(filter.getPage(), filter.getLimit(), filter.getSortBy());
        return repository.findAll(new CheckListItemsSpecification(filter), pagination)
                .stream().map(mapper::toResponse).toList();
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
        return mapper.toResponse(repository.save(item));
    }

    @Override
    @Transactional(readOnly = true)
    public CheckListItemsResponse getOne(Long id) {
        return mapper.toResponse(
                repository.findById(id).orElseThrow(() -> new CustomNotFoundException(msgcode))
        );
    }

    @Override
    @Transactional
    public Boolean delete(Long id) {
        CheckListItem item = repository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException(msgcode));
        item.makeAsDeleted();
        repository.save(item);
        return true;
    }
}