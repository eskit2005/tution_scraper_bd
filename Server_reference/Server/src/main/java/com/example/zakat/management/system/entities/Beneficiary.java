package com.example.zakat.management.system.entities;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "beneficiary")
@DiscriminatorValue("BENEFICIARY")
public class Beneficiary extends User {

    @Column(name = "reason", columnDefinition = "TEXT")
    private String reason;

    @Column(name = "dependents")
    private Integer dependents;

    @Column(name = "income", precision = 12, scale = 2)
    private BigDecimal income;

    @Column(name = "emergency")
    private Boolean emergency;

    @Column(name = "disability")
    private Boolean disability;

    @Column(name = "age")
    private Integer age;

    @Column(name = "is_orphan")
    private Boolean isOrphan;

    @Column(name = "has_debt")
    private Boolean hasDebt;

    @Column(name = "unemployed")
    private Boolean unemployed;

    @Column(name = "illness")
    private Boolean illness;

    @Column(name = "priority_score")
    private Integer priorityScore;

    @Column(name = "eligible")
    private Boolean eligible;

    @Column(name = "reject_reason", columnDefinition = "TEXT")
    private String rejectReason;

    @Column(name = "checked_at")
    private Instant checkedAt;

    @Column(name = "total_received_value", precision = 12, scale = 2)
    private BigDecimal totalReceivedValue = BigDecimal.ZERO;
}