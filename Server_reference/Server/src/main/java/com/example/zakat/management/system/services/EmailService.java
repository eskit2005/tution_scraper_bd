package com.example.zakat.management.system.services;

import com.example.zakat.management.system.entities.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    public void sendRegistrationEmail(String to, String role,String name) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Registration Email");
        if(role.equals(Role.BENEFICIARY.name())){
            message.setText("Thanks for signing up "+name+" , You are now a  beneficiary.");
        }
        else if(role.equals(Role.ADMIN.name())){
            message.setText("Thanks for signing up "+name+" , we are thrilled at the prospect of stealing your money in the form of a donation ");
        }
        else{
            message.setText("Welcome dear admin, we are thrilled to have you ");
        }

        javaMailSender.send(message);

    }

    public void sendZakatEmail(String to, BigDecimal zakatAssigned,String name) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Zakat received");
        message.setText("Hi "+name+", We are glad to inform you that you have received "+ zakatAssigned+ "Tk as Zakat from our evil organization");
        javaMailSender.send(message);

    }

}
