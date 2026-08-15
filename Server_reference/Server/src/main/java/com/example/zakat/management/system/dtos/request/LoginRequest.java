package com.example.zakat.management.system.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank
    @Email(message = "Please provide a valid email")
    private String email;

    @NotBlank
    @Size(min = 1, max = 255)
    private String password;
}
