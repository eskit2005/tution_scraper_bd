package com.example.zakat.management.system.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ItemDonationRequest {

    @NotNull(message = "Donor ID is required")
    private Long donorId;

    @NotBlank(message = "Item name is required")
    private String name;

    @NotNull(message = "Approximate value is required")
    @Positive(message = "Approximate value must be positive")
    private BigDecimal appoxValue;
}