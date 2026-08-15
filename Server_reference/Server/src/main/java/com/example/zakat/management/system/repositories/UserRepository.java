package com.example.zakat.management.system.repositories;

import com.example.zakat.management.system.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    int countByEmail(String email);

    long countByRole(String role);
}