package com.example.zakat.management.system.dtos.response;

import lombok.Data;
import java.time.Instant;

@Data
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private String role;
    private Instant createdAt;
}
