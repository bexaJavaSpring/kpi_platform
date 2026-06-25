package uz.java.kpisystem.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.java.kpisystem.dto.ApiResponse;
import uz.java.kpisystem.dto.project.ProjectFilter;
import uz.java.kpisystem.dto.project.ProjectInfo;
import uz.java.kpisystem.dto.project.ProjectRequest;
import uz.java.kpisystem.entity.Group;
import uz.java.kpisystem.entity.Organization;
import uz.java.kpisystem.entity.Project;
import uz.java.kpisystem.event.GenericCacheEvictEvent;
import uz.java.kpisystem.exception.CustomNotFoundException;
import uz.java.kpisystem.exception.RedisNotSerializableException;
import uz.java.kpisystem.listener.CacheEvictEventListener;
import uz.java.kpisystem.mapper.ProjectMapper;
import uz.java.kpisystem.repository.GroupRepository;
import uz.java.kpisystem.repository.OrganizationRepository;
import uz.java.kpisystem.repository.ProjectRepository;
import uz.java.kpisystem.util.CachePrefix;

import java.util.List;
import java.util.Optional;

@Service
public class ProjectService implements IProjectService {
    private final ProjectRepository repository;
    private final ProjectMapper mapper;
    private final OrganizationRepository organizationRepository;
    private final GroupRepository groupRepository;
    private final CacheManagerService cacheManagerService;
    private final CacheEvictEventListener cacheEvictEventListener;


    public ProjectService(ProjectRepository repository, ProjectMapper mapper, OrganizationRepository organizationRepository, GroupRepository groupRepository, CacheManagerService cacheManagerService, CacheEvictEventListener cacheEvictEventListener) {
        this.repository = repository;
        this.mapper = mapper;
        this.organizationRepository = organizationRepository;
        this.groupRepository = groupRepository;
        this.cacheManagerService = cacheManagerService;
        this.cacheEvictEventListener = cacheEvictEventListener;
    }

    @Override
    @Transactional(readOnly = true)
        public ApiResponse<List<ProjectInfo>> getAll(ProjectFilter projectFilter) {
        Object data = cacheManagerService.get(String.valueOf(projectFilter.hashCode()), CachePrefix.PROJECT);
        if (data != null) {
            return (ApiResponse<List<ProjectInfo>>) data;
        }
        List<Project> all = repository.findAllCustom();
        List<ProjectInfo> response = all.stream().map(mapper::toResponse).toList();
        cacheManagerService.put(String.valueOf(projectFilter.hashCode()), CachePrefix.PROJECT, new ApiResponse<>(response));
        return new ApiResponse<>(response);
    }

    @Override
    @Transactional
    public Long create(ProjectRequest request) {
        Project project = mapper.toEntity(request);

        Organization org = organizationRepository.findById(request.getOrganizationId())
                .orElseThrow(() -> new CustomNotFoundException("Organization not found"));
        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new CustomNotFoundException("Group not found"));

        project.setOrganization(org);
        project.setGroup(group);
        repository.save(project);
        cacheEvictEventListener.handleCacheEvict(new GenericCacheEvictEvent<ProjectService>(CachePrefix.PROJECT));
        return project.getId();
    }

    @Override
    @Transactional
    public ProjectInfo update(Long id, ProjectRequest request) {
        Project project = repository.findById(id)
                .orElseThrow(() -> new CustomNotFoundException("Project not found"));

        mapper.updateFromRequest(request, project);

        if (request.getOrganizationId() != null) {
            Organization org = organizationRepository.findById(request.getOrganizationId())
                    .orElseThrow(() -> new CustomNotFoundException("Organization not found"));
            project.setOrganization(org);
        }
        if (request.getGroupId() != null) {
            Group group = groupRepository.findById(request.getGroupId())
                    .orElseThrow(() -> new CustomNotFoundException("Group not found"));
            project.setGroup(group);
        }

        repository.save(project);
        cacheEvictEventListener.handleCacheEvict(new GenericCacheEvictEvent(CachePrefix.PROJECT));
        return getOne(id);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectInfo getOne(Long id) {
        Object data = cacheManagerService.get(id.toString(), CachePrefix.PROJECT);
        if (data != null)
            return (ProjectInfo) data;
        Optional<Project> opt = repository.findById(id);
        if (!opt.isPresent())
            throw new CustomNotFoundException("Project not found");

        Project project = opt.get();
        ProjectInfo info = mapper.toResponse(project);
        try {
            cacheManagerService.put(id.toString(), CachePrefix.PROJECT, info);
        } catch (Exception e) {
            throw new RedisNotSerializableException(e.getMessage());
        }
        return info;
    }

    @Override
    @Transactional
    public Boolean delete(Long id) {
        Project project = repository.findById(id).orElseThrow(() -> new CustomNotFoundException("Project not found"));
        project.makeAsDeleted();
        repository.save(project);
        // todo project o'chsa undagi hamma task lar ham o'chadi va cachedan ham unga bogliq hamma entity malumotlari o'chishi kk
        cacheEvictEventListener.handleCacheEvict(new GenericCacheEvictEvent(CachePrefix.PROJECT));
        return true;
    }

}
