package uz.java.kpisystem.service;

import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uz.java.kpisystem.dto.ApiResponse;
import uz.java.kpisystem.dto.group.GroupResponse;
import uz.java.kpisystem.dto.organization.OrganizationFilter;
import uz.java.kpisystem.dto.organization.OrganizationInfo;
import uz.java.kpisystem.dto.organization.OrganizationRequest;
import uz.java.kpisystem.entity.Organization;
import uz.java.kpisystem.event.OrganizationCacheEvictEvent;
import uz.java.kpisystem.exception.CustomNotFoundException;
import uz.java.kpisystem.exception.RedisNotSerializableException;
import uz.java.kpisystem.listener.CacheEvictEventListener;
import uz.java.kpisystem.mapper.OrganizationMapper;
import uz.java.kpisystem.repository.OrganizationRepository;
import uz.java.kpisystem.specifications.GroupSpecification;
import uz.java.kpisystem.specifications.OrganizationSpecification;
import uz.java.kpisystem.specifications.SearchSpecification;
import uz.java.kpisystem.util.CachePrefix;

import java.util.List;
import java.util.Optional;

@Service  // bean qilib beradi
public class OrganizationService implements IOrganizationService {

    private final OrganizationRepository repository;
    private final OrganizationMapper mapper;
    private final  CacheManagerService cacheManagerService;
    private final CacheEvictEventListener cacheEvictEventListener;

    public OrganizationService(OrganizationRepository repository, OrganizationMapper mapper,CacheManagerService cacheManagerService, CacheEvictEventListener cacheEvictEventListener) {
        this.repository = repository;
        this.mapper = mapper;
        this.cacheManagerService = cacheManagerService;
        this.cacheEvictEventListener = cacheEvictEventListener;
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<OrganizationInfo>> getAll(OrganizationFilter organizationFilter) {
        Object data = cacheManagerService.get(String.valueOf(organizationFilter.hashCode()), CachePrefix.ORGANIZATIONS);
        if(data != null) {
            return (ApiResponse<List<OrganizationInfo>>) data;
        }
        OrganizationSpecification spec = new OrganizationSpecification(organizationFilter);
        Pageable pagination = SearchSpecification.getPageable(organizationFilter.getPage(), organizationFilter.getLimit(),
                organizationFilter.getSortBy());
        List<OrganizationInfo> response = repository.findAll(spec, pagination).stream().map(mapper::toResponse).toList();
        ApiResponse<List<OrganizationInfo>> listApiResponse = new ApiResponse<>(response);
        cacheManagerService.put(String.valueOf(organizationFilter.hashCode()), CachePrefix.ORGANIZATIONS, listApiResponse);
        return listApiResponse;
    }

    @Override
    @Transactional
    public Long create(OrganizationRequest request) {
        Organization organization = mapper.toEntity(request);
        Organization save = repository.save(organization);
        cacheEvictEventListener.handleCacheEvict(new OrganizationCacheEvictEvent(CachePrefix.ORGANIZATIONS));
        return save.getId();
    }

    @Override
    @Transactional
    public OrganizationInfo update(Long id, OrganizationRequest request) {
        Optional<Organization> opt = repository.findById(id);
        if (!opt.isPresent())
            throw new CustomNotFoundException("Organization not found");
        Organization organization = opt.get();
        mapper.updateFromRequest(request, organization);
        repository.save(organization);
        cacheEvictEventListener.handleCacheEvict(new OrganizationCacheEvictEvent(CachePrefix.ORGANIZATIONS));
        return getOne(id);
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationInfo getOne(Long id) {
        Object data = cacheManagerService.get(id.toString(), CachePrefix.ORGANIZATIONS);
        if (data != null)
            return (OrganizationInfo) data;

        Optional<Organization> opt = repository.findById(id);
        if (!opt.isPresent())
            throw new CustomNotFoundException("Organization not found");

        Organization organization = opt.get();
        OrganizationInfo response = mapper.toResponse(organization);
        try {
            cacheManagerService.put(id.toString(), CachePrefix.ORGANIZATIONS, response);
        } catch (Exception e) {
            throw new RedisNotSerializableException(e.getMessage());
        }

        return response;
    }

    @Override
    public Boolean delete(Long id) {
//        Organization organization = repository.findById(id).orElse(null);
        Organization organization = repository.findById(id).orElseThrow(() -> new CustomNotFoundException("Organization not found"));
//        repository.delete(organization); // hard delete
        organization.makeAsDeleted();
        repository.save(organization); // soft delete
        cacheEvictEventListener.handleCacheEvict(new OrganizationCacheEvictEvent(CachePrefix.ORGANIZATIONS));
        return true;
    }

//    EPAM interview:
//    1) Optional class of(), ofNullable() methodlari
//    2) stream Api
//    3) Functional interface ozi nima va ichida qanaqa method lar yaratsa boladi
}
