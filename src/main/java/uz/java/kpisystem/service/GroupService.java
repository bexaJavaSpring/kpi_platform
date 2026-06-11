package uz.java.kpisystem.service;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.java.kpisystem.dto.group.GroupFilter;
import uz.java.kpisystem.dto.group.GroupResponse;
import uz.java.kpisystem.entity.Group;
import uz.java.kpisystem.exception.CustomNotFoundException;
import uz.java.kpisystem.mapper.GroupMapper;
import uz.java.kpisystem.repository.GroupRepository;
import uz.java.kpisystem.specifications.GroupSpecification;
import uz.java.kpisystem.specifications.SearchSpecification;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class GroupService implements IGroupService {
    private final GroupRepository repository;
    private final GroupMapper mapper;
    private final String msgcode = "group.not.found";

    public GroupService(GroupRepository repository, GroupMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public List<GroupResponse> getAll(GroupFilter groupFilter) {
        GroupSpecification spec = new GroupSpecification(groupFilter);
        Pageable pagination = SearchSpecification.getPageable(groupFilter.getPage(), groupFilter.getLimit(),
                groupFilter.getSortBy());
        return repository.findAll(spec, pagination).stream().map(mapper::toResponse).toList();
    }

    @Override
    @Transactional
    public Long create(String name) {
//        Group group = new Group();
//        group.setName(name);
        Group build = Group.builder().name(name).build();
        Group save = repository.save(build);
        return save.getId();
    }

    @Override
    public Long update(Long id, String name) {
        Optional<Group> opt = repository.findById(id);
        if (!opt.isPresent())
            throw new CustomNotFoundException(msgcode);
        Group group = opt.get();
        group.setName(name);
        Group save = repository.save(group);
        return save.getId();
    }

    @Override
    public GroupResponse getOne(Long id) {
        Optional<Group> opt = repository.findById(id);
        if (!opt.isPresent())
            throw new CustomNotFoundException(msgcode);

        Group group = opt.get();
        return mapper.toResponse(group);
    }

    @Override
    public Boolean delete(Long id) {
        Group group = repository.findById(id).orElseThrow(
                () -> new CustomNotFoundException(msgcode)
        );
        group.makeAsDeleted();
        repository.save(group);
        return true;
    }
}
