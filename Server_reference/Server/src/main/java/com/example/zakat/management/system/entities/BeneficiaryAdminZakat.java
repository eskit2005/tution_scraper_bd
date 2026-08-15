package com.example.zakat.management.system.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "beneficiary_admin_zakat")
public class BeneficiaryAdminZakat {

    @EmbeddedId
    private BeneficiaryAdminZakatId id=new BeneficiaryAdminZakatId();

    @MapsId("zId")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "z_id", insertable = false, updatable = false)
    private ZakatAssignment assignment;

    @MapsId("bId")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "b_id", insertable = false, updatable = false)
    private Beneficiary beneficiary;

    @MapsId("aId")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "a_id", insertable = false, updatable = false)
    private Admin admin;
}