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
@Table(name = "zakat_assignment")
public class ZakatAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "amount_assigned")
    private BigDecimal amountAssigned;

    @CreationTimestamp
    @Column(name = "assigned_at", insertable = false, updatable = false)
    private Instant assignedAt;

    @OneToOne()
    @JoinColumn(name = "inven_id")
    private Inventory inventory;
}