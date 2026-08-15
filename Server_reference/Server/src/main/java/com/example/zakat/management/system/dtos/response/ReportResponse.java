package com.example.zakat.management.system.dtos.response;

import lombok.Data;

@Data
public class ReportResponse {
    private Double totalDonated;
    private Double totalDistributed;
    private Double remaining;
    private Long totalDonors;
    private Long totalBeneficiaries;
    private Long totalAssignments;
}