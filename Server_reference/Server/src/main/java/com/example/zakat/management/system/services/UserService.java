package com.example.zakat.management.system.services;

import com.example.zakat.management.system.dtos.request.RegisterRequest;
import com.example.zakat.management.system.dtos.response.UserResponse;
import com.example.zakat.management.system.entities.Admin;
import com.example.zakat.management.system.entities.Beneficiary;
import com.example.zakat.management.system.entities.Donor;
import com.example.zakat.management.system.entities.User;
import com.example.zakat.management.system.events.UserRegisteredEvent;
import com.example.zakat.management.system.exceptions.DuplicateResourceException;
import com.example.zakat.management.system.exceptions.ResourceNotFoundException;
import com.example.zakat.management.system.mappers.UserMapper;
import com.example.zakat.management.system.repositories.AdminRepository;
import com.example.zakat.management.system.repositories.BeneficiaryRepository;
import com.example.zakat.management.system.repositories.DonorRepository;
import com.example.zakat.management.system.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final UserRepository userRepository;
    private final AdminRepository adminRepository;
    private final DonorRepository donorRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.countByEmail(request.getEmail()) > 0) {
            throw new DuplicateResourceException("Email already in use: " + request.getEmail());
        }

        String role = request.getRole();
        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User saved;

        if ("ADMIN".equals(role)) {
            Admin admin = new Admin();
            admin.setName(request.getName());
            admin.setEmail(request.getEmail());
            admin.setPassword(encodedPassword);
            admin.setRole(role);
            saved = adminRepository.save(admin);
        } else if ("DONOR".equals(role)) {
            Donor donor = new Donor();
            donor.setName(request.getName());
            donor.setEmail(request.getEmail());
            donor.setPassword(encodedPassword);
            donor.setRole(role);
            saved = donorRepository.save(donor);
        } else if ("BENEFICIARY".equals(role)) {
            Beneficiary beneficiary = new Beneficiary();
            beneficiary.setName(request.getName());
            beneficiary.setEmail(request.getEmail());
            beneficiary.setPassword(encodedPassword);
            beneficiary.setRole(role);
            saved = beneficiaryRepository.save(beneficiary);
        } else {
            throw new IllegalArgumentException("Invalid role: " + role);
        }

        applicationEventPublisher.publishEvent(new UserRegisteredEvent(saved.getName(), saved.getEmail(), role));

        return userMapper.toResponse(saved);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toResponse)
                .toList();
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
}
