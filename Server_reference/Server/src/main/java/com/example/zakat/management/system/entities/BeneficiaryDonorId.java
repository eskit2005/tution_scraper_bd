package com.example.zakat.management.system.entities;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class BeneficiaryDonorId implements Serializable {
    private Long DId;
    private Long BId;
}