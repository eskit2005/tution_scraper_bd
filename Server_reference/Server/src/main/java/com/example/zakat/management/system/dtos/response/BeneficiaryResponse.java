package com.example.zakat.management.system.dtos.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data
public class BeneficiaryResponse {
    private Long id;
    private Long userId;
    private String fullName;
    private String reason;
    private Integer dependents;
    private BigDecimal income;
    private Boolean emergency;
    private Boolean disability;
    private Integer age;
    private Boolean isOrphan;
    private Boolean hasDebt;
    private Boolean unemployed;
    private Boolean illness;
    private Integer priorityScore;
    private Boolean eligible;
    private String rejectReason;
    private BigDecimal totalReceivedValue;
    private Instant checkedAt;
}