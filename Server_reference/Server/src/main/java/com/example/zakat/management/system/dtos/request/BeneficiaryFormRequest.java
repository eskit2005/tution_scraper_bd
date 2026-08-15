package com.example.zakat.management.system.dtos.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class BeneficiaryFormRequest {

    @NotNull
    private Long beneficiaryId;

    private String reason;

    @PositiveOrZero
    private Integer dependents;

    @PositiveOrZero
    private BigDecimal income;

    private Boolean emergency;

    private Boolean disability;

    @PositiveOrZero
    private Integer age;

    private Boolean isOrphan;

    private Boolean hasDebt;

    private Boolean unemployed;

    private Boolean illness;
}