package com.example.zakat.management.system.events;

import com.example.zakat.management.system.services.EmailService;
import lombok.AllArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class UserRegisteredEventListener {

    private final EmailService emailService;

    @Async
    @EventListener
    public void onUserRegistered(UserRegisteredEvent event) {
        emailService.sendRegistrationEmail(event.getEmail(), event.getRole(), event.getName());
    }
}
