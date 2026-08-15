package com.example.zakat.management.system.dtos.response;

import lombok.Data;
import java.time.Instant;

@Data
public class DonorResponse {
    private Long id;
    private String name;
    private String email;
    private Instant createdAt;
}