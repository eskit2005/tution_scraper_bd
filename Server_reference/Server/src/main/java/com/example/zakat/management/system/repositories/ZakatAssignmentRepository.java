package com.example.zakat.management.system.repositories;

import com.example.zakat.management.system.entities.ZakatAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ZakatAssignmentRepository extends JpaRepository<ZakatAssignment, Long> {

    @Query(value = "SELECT * FROM zakat_assignment WHERE id = :id", nativeQuery = true)
    Optional<ZakatAssignment> findById(@Param("id") Long id);

    @Query(value = "SELECT * FROM zakat_assignment", nativeQuery = true)
    List<ZakatAssignment> findAll();

    @Query(value = "SELECT COALESCE(SUM(amount_assigned), 0) FROM zakat_assignment WHERE inven_id IS NULL", nativeQuery = true)
    Double sumAllAmounts();

    @Query(value = "SELECT * FROM zakat_assignment WHERE inven_id = :invenId", nativeQuery = true)
    List<ZakatAssignment> findByInventoryId(@Param("invenId") Long invenId);
}