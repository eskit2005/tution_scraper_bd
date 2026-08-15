package com.example.zakat.management.system.entities;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Embeddable
public class BeneficiaryAdminZakatId implements Serializable {
    private Long zId;
    private Long bId;
    private Long aId;
}