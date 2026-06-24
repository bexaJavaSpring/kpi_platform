package uz.java.kpisystem.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import uz.java.kpisystem.dto.ApiResponse;
import uz.java.kpisystem.dto.organization.OrganizationFilter;
import uz.java.kpisystem.dto.organization.OrganizationInfo;
import uz.java.kpisystem.dto.organization.OrganizationRequest;
import uz.java.kpisystem.entity.Organization;
import uz.java.kpisystem.event.OrganizationCacheEvictEvent;
import uz.java.kpisystem.exception.CustomNotFoundException;
import uz.java.kpisystem.exception.FileNotFoundException;
import uz.java.kpisystem.exception.RedisNotSerializableException;
import uz.java.kpisystem.listener.CacheEvictEventListener;
import uz.java.kpisystem.mapper.OrganizationMapper;
import uz.java.kpisystem.repository.OrganizationRepository;
import uz.java.kpisystem.specifications.OrganizationSpecification;
import uz.java.kpisystem.specifications.SearchSpecification;
import uz.java.kpisystem.util.CachePrefix;

import java.util.List;
import java.util.Optional;

@Service  // bean qilib beradi
@RequiredArgsConstructor
public class OrganizationService implements IOrganizationService {

    private final OrganizationRepository repository;
    private final OrganizationMapper mapper;
    private final  CacheManagerService cacheManagerService;
    private final CacheEvictEventListener cacheEvictEventListener;
    private  final FileService fileService;


    @Override
    @Transactional(readOnly = true)
    public ApiResponse<List<OrganizationInfo>> getAll(OrganizationFilter organizationFilter) {
        Object data = cacheManagerService.get(String.valueOf(organizationFilter.hashCode()), CachePrefix.ORGANIZATIONS);
        if(data != null) {
            ApiResponse<List<OrganizationInfo>> cached = (ApiResponse<List<OrganizationInfo>>) data;
            cached.getData().forEach(this::enrichLogo);
            return cached;
        }
        OrganizationSpecification spec = new OrganizationSpecification(organizationFilter);
        Pageable pagination = SearchSpecification.getPageable(organizationFilter.getPage(), organizationFilter.getLimit(),
                organizationFilter.getSortBy());
        List<OrganizationInfo> response = repository.findAll(spec, pagination).stream().map(mapper::toResponse).toList();
        ApiResponse<List<OrganizationInfo>> listApiResponse = new ApiResponse<>(response);
        cacheManagerService.put(String.valueOf(organizationFilter.hashCode()), CachePrefix.ORGANIZATIONS, listApiResponse);
        response.forEach(this::enrichLogo);
        return listApiResponse;
    }

    @Override
    @Transactional
    public Long create(OrganizationRequest request) {
        validateLogo(request.getLogo());
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
        validateLogo(request.getLogo());
        String oldLogo = organization.getLogo();
        mapper.updateFromRequest(request, organization);
        repository.save(organization);
        String newLogo = organization.getLogo();
        if(!oldLogo.equals(newLogo)) {
            if (StringUtils.hasText(oldLogo) && !oldLogo.equals(newLogo))
                fileService.deleteFile(oldLogo);
        }
        cacheEvictEventListener.handleCacheEvict(new OrganizationCacheEvictEvent(CachePrefix.ORGANIZATIONS));
        return getOne(id);
    }

    @Override
    @Transactional(readOnly = true)
    public OrganizationInfo getOne(Long id) {
        Object data = cacheManagerService.get(id.toString(), CachePrefix.ORGANIZATIONS);
        if (data != null)
            return enrichLogo((OrganizationInfo) data);

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

        return enrichLogo(response);
    }

    private OrganizationInfo enrichLogo(OrganizationInfo info) {
        if (info != null) {
            info.setLogoForImage(fileService.getPresignedUrl(info.getLogo()));
        }
        return info;
    }

    private void validateLogo(String logo) {
        if (StringUtils.hasText(logo) && !fileService.exists(logo)) {
            throw new FileNotFoundException("organization.logo.not.found");
        }
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
