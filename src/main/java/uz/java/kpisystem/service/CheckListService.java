package uz.java.kpisystem.service;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.java.kpisystem.dto.checkList.CheckListFilter;
import uz.java.kpisystem.dto.checkList.CheckListRequest;
import uz.java.kpisystem.dto.checkList.CheckListResponse;
import uz.java.kpisystem.entity.CheckList;
import uz.java.kpisystem.exception.CustomNotFoundException;
import uz.java.kpisystem.mapper.CheckListMapper;
import uz.java.kpisystem.repository.CheckListRepository;
import uz.java.kpisystem.specifications.CheckListSpecification;
import uz.java.kpisystem.specifications.SearchSpecification;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CheckListService implements ICheckListService {
    private final CheckListRepository repository;
    private final CheckListMapper mapper;
    private final String msgcode = "checklist.not.found";

    @Override
    @Transactional(readOnly = true)
   public List<CheckListResponse> getAll(CheckListFilter checkListFilter) {
        CheckListSpecification spec = new CheckListSpecification(checkListFilter);
        Pageable pagination = SearchSpecification.getPageable(checkListFilter.getPage(), checkListFilter.getLimit(),
                checkListFilter.getSortBy());
        return repository.findAll(spec, pagination).stream().map(mapper::toResponse).toList();
    };

    @Override
    public Long create(CheckListRequest request) {
        CheckList checkList = mapper.toEntity(request);
        CheckList save = this.repository.save(checkList);
        return save.getId();
    };


    @Override
    public CheckListResponse update(Long id,CheckListRequest request){
        Optional<CheckList> opt = repository.findById(id);
        if (!opt.isPresent())
            throw new CustomNotFoundException(msgcode);
        CheckList checkList = opt.get();
        mapper.updateFromRequest(request, checkList);
        repository.save(checkList);
        return getOne(id);
    };

    @Override
    public CheckListResponse getOne(Long id) {
        Optional<CheckList> opt = repository.findById(id);
        if (!opt.isPresent())
            throw new CustomNotFoundException(msgcode);
        CheckList checkList = opt.get();
        return mapper.toResponse(checkList);
    }

    @Override
    public Boolean delete(Long id){
        CheckList checkList = repository.findById(id).orElseThrow(() -> new CustomNotFoundException(msgcode));
        checkList.makeAsDeleted();
        repository.save(checkList);
        return true;
    };

}
