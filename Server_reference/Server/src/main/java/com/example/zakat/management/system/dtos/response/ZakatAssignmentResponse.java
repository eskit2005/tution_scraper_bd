package com.example.zakat.management.system.dtos.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data
public class ZakatAssignmentResponse {
    private BigDecimal amountAssigned;
    private Instant assignedAt;
}