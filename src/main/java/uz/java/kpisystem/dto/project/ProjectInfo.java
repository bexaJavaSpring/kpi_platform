package uz.java.kpisystem.dto.project;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import uz.java.kpisystem.dto.organization.OrganizationInfo;
import uz.java.kpisystem.entity.enums.ProjectStatus;

@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class ProjectInfo {
    private Long id;
    private String name;
    private String description;
    private String startDate;
    private String endDate;
    private ProjectStatus status;
    private OrganizationInfo organization;
//    private GroupResponse group;
    private boolean deleted;
}

