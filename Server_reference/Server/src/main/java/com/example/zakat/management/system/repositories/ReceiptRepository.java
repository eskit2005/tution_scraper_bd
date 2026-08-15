package com.example.zakat.management.system.repositories;

import com.example.zakat.management.system.entities.Receipt;
import com.example.zakat.management.system.entities.ReceiptId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, ReceiptId> {

    @Query(value = "SELECT * FROM receipt WHERE d_id = :donorId AND recep_num = :recepNum", nativeQuery = true)
    Optional<Receipt> findById(@Param("donorId") Long donorId, @Param("recepNum") String recepNum);

    @Query(value = "SELECT * FROM receipt", nativeQuery = true)
    List<Receipt> findAll();

    @Query(value = "SELECT * FROM receipt WHERE d_id = :donorId", nativeQuery = true)
    List<Receipt> findAllByDonorIdDirectly(@Param("donorId") Long donorId);

    @Query(value = "SELECT * FROM receipt WHERE recep_num = :recepNum", nativeQuery = true)
    Optional<Receipt> findByRecepNum(@Param("recepNum") String recepNum);

    @Query(value = "SELECT COALESCE(SUM(amount), 0) FROM receipt", nativeQuery = true)
    Double sumAllAmounts();

    @Query(value = "SELECT COALESCE(SUM(amount), 0) FROM receipt WHERE d_id = :donorId", nativeQuery = true)
    Double sumAmountsByDonorId(@Param("donorId") Long donorId);
}