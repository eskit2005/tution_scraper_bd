package com.example.zakat.management.system.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    private String password;

    @NotBlank
    @Pattern(regexp = "DONOR|BENEFICIARY|ADMIN", message = "Role must be DONOR, BENEFICIARY or ADMIN")
    private String role;
}
