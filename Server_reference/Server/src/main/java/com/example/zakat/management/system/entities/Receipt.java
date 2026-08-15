package com.example.zakat.management.system.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "receipt")
public class Receipt {

    @EmbeddedId
    private ReceiptId receiptId = new ReceiptId();

    @MapsId("dId")
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "d_id")
    private Donor donor;

    @Column(name = "recep_num", unique = true)
    private String recepNum;

    @Column(name = "amount", nullable = false)
    private BigDecimal amount;

    @CreationTimestamp
    @Column(name = "issued_at", insertable = false, updatable = false)
    private Instant issuedAt;

    @PrePersist
    public void prePersist() {
        if (receiptId.getDId() == null && donor != null) {
            receiptId.setDId(donor.getId());
        }
    }
}