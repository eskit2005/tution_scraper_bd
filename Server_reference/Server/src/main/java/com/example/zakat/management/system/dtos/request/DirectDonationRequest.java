package com.example.zakat.management.system.dtos.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class DirectDonationRequest {
    // @NotNull(message = "Donor ID is required")
    // private Long donorId;

    @NotNull(message = "Beneficiary ID is required")
    private Long beneficiaryId;

    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
}