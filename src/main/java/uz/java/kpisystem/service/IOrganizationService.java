package uz.java.kpisystem.service;

import uz.java.kpisystem.dto.ApiResponse;
import uz.java.kpisystem.dto.organization.OrganizationFilter;
import uz.java.kpisystem.dto.organization.OrganizationInfo;
import uz.java.kpisystem.dto.organization.OrganizationRequest;

import java.util.List;

public interface IOrganizationService {
    ApiResponse< List<OrganizationInfo>> getAll(OrganizationFilter organizationFilter);

    Long create(OrganizationRequest request);

    OrganizationInfo update(Long id, OrganizationRequest request);

    OrganizationInfo getOne(Long id);

    Boolean delete(Long id);
}
