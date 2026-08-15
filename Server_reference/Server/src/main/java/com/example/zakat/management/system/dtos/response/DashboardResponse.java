package com.example.zakat.management.system.dtos.response;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DashboardResponse {
    private Long totalDonors;
    private Long totalBeneficiaries;
    private Double totalDonated;
    private Double totalDistributed;
    private Double remaining;
}