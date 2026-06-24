package uz.java.kpisystem.dto.organization;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class OrganizationInfo {
    private Long id;
    private String name;
    private String address;
    private String phoneNumber;
    private String email;
    private String website;
    private String logo;
    private String logoForImage;
}
