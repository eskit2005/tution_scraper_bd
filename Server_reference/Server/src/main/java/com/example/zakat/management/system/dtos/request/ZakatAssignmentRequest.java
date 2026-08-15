package com.example.zakat.management.system.dtos.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ZakatAssignmentRequest {

    @NotNull
    private Long beneficiaryId;

    @NotNull
    private Long adminId;

    @DecimalMin(value = "1.00", message = "Assignment amount must be at least 1.00")
    private BigDecimal amountAssigned;

    private Long inventoryId;
}