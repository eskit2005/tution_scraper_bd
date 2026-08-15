package com.example.zakat.management.system.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
public class ZakatAssignedEvent {
    private final String email;
    private final String name;
    private final BigDecimal amount;
}
