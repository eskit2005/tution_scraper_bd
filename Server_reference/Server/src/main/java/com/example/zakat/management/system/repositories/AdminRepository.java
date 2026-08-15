package com.example.zakat.management.system.repositories;

import com.example.zakat.management.system.entities.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    @Query(value = "SELECT u.id, u.name, u.email, u.password, u.role, u.created_at FROM users u JOIN admin a ON u.id = a.id WHERE u.id = :id", nativeQuery = true)
    Optional<Admin> findById(@Param("id") Long id);

    @Query(value = "SELECT u.id, u.name, u.email, u.password, u.role, u.created_at FROM users u JOIN admin a ON u.id = a.id", nativeQuery = true)
    List<Admin> findAll();
}