package com.example.zakat.management.system.repositories;

import com.example.zakat.management.system.entities.Donor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DonorRepository extends JpaRepository<Donor, Long> {

    @Query(value = "SELECT u.id, u.name, u.email, u.password, u.role, u.created_at FROM users u JOIN donor d ON u.id = d.id WHERE u.id = :id", nativeQuery = true)
    Optional<Donor> findById(@Param("id") Long id);

    @Query(value = "SELECT u.id, u.name, u.email, u.password, u.role, u.created_at FROM users u JOIN donor d ON u.id = d.id", nativeQuery = true)
    List<Donor> findAll();
}