package com.example.zakat.management.system.dtos.response;

import lombok.Data;
import java.math.BigDecimal;
import java.time.Instant;

@Data
public class DonationResponse {
    private Long receiptId;
    private String recepNum;
    private String donorName;
    private BigDecimal amount;
    private String description;
    private Instant issuedAt;
}