package com.example.zakat.management.system.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "refresh_tokens")
public class RefreshToken {

    @Id
    @Column(name = "token", length = 255)
    private String token;

    @Column(name = "issued_at", nullable = false)
    private Instant issuedAt;

    @Column(name = "expiry", nullable = false)
    private Instant expiry;

    @Column(name = "revoked", nullable = false)
    private Boolean revoked;

    @Column(name = "user_id", nullable = false)
    private Long userId;
}
