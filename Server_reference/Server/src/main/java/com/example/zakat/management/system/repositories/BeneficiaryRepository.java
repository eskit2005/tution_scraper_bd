package com.example.zakat.management.system.repositories;

import com.example.zakat.management.system.entities.Beneficiary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BeneficiaryRepository extends JpaRepository<Beneficiary, Long> {

    @Query(value = "SELECT u.id, u.name, u.email, u.password, u.role, u.created_at, b.* FROM users u JOIN beneficiary b ON u.id = b.id WHERE u.id = :id", nativeQuery = true)
    Optional<Beneficiary> findById(@Param("id") Long id);

    @Query(value = "SELECT u.id, u.name, u.email, u.password, u.role, u.created_at, b.* FROM users u JOIN beneficiary b ON u.id = b.id", nativeQuery = true)
    List<Beneficiary> findAll();

    @Query(value = "SELECT COUNT(*) FROM beneficiary", nativeQuery = true)
    long count();

    @Query(value = "SELECT COUNT(*) FROM beneficiary WHERE id = :id", nativeQuery = true)
    int countById(@Param("id") Long id);

    @Query(value = "SELECT u.id, u.name, u.email, u.password, u.role, u.created_at, b.* FROM users u JOIN beneficiary b ON u.id = b.id WHERE b.eligible = TRUE ORDER BY b.priority_score DESC", nativeQuery = true)
    List<Beneficiary> findByEligibleTrueOrderByPriorityScoreDesc();

    @Query(value = "SELECT u.id, u.name, u.email, u.password, u.role, u.created_at, b.* FROM users u JOIN beneficiary b ON u.id = b.id WHERE b.checked_at IS NULL", nativeQuery = true)
    List<Beneficiary> findByCheckedAtIsNull();
}