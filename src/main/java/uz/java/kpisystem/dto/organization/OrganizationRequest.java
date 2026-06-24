package uz.java.kpisystem.dto.organization;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.jetbrains.annotations.NotNull;

@Data
public class OrganizationRequest {
    @NotBlank(message = "organization.name.must.not.be.blank")
    private String name;
    private String address;
//    @Pattern(regexp = "")
    private String phone;
    @Email
    private String email;
    private String website;
    @NotBlank(message = "organization.logo.must.not.be.blank")
    private String logo;
}
