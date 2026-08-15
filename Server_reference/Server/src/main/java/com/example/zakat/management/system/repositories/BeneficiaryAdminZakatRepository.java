package com.example.zakat.management.system.repositories;

import com.example.zakat.management.system.entities.BeneficiaryAdminZakat;
import com.example.zakat.management.system.entities.BeneficiaryAdminZakatId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BeneficiaryAdminZakatRepository extends JpaRepository<BeneficiaryAdminZakat, BeneficiaryAdminZakatId> {

    @Query(value = "SELECT * FROM beneficiary_admin_zakat WHERE b_id = :bId", nativeQuery = true)
    List<BeneficiaryAdminZakat> findByIdBId(@Param("bId") Long bId);

    @Query(value = "SELECT * FROM beneficiary_admin_zakat WHERE a_id = :aId", nativeQuery = true)
    List<BeneficiaryAdminZakat> findByIdAId(@Param("aId") Long aId);

    @Query(value = "SELECT * FROM beneficiary_admin_zakat", nativeQuery = true)
    List<BeneficiaryAdminZakat> findAll();
}