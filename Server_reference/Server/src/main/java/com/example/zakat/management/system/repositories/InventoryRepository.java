package com.example.zakat.management.system.repositories;

import com.example.zakat.management.system.entities.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    @Query(value = "SELECT * FROM inventory WHERE id = :id", nativeQuery = true)
    Optional<Inventory> findById(@Param("id") Long id);

    @Query(value = "SELECT * FROM inventory", nativeQuery = true)
    List<Inventory> findAll();

    @Query(value = "SELECT * FROM inventory WHERE d_id = :dId", nativeQuery = true)
    List<Inventory> findByDonorId(@Param("dId") Long dId);

    @Query(value = "SELECT * FROM inventory WHERE status = :status", nativeQuery = true)
    List<Inventory> findByStatus(@Param("status") String status);
}