package com.example.zakat.management.system.events;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserRegisteredEvent {
    private final String name;
    private final String email;
    private final String role;
}
