package com.example.zakat.management.system.dtos.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data
public class InventoryResponse {
    private Long id;
    private String name;
    private BigDecimal appoxValue;
    private String status;
    private Instant receivedAt;
    private Long donorId;
}