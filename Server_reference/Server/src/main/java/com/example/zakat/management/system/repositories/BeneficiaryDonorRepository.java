package com.example.zakat.management.system.repositories;

import com.example.zakat.management.system.entities.BeneficiaryDonor;
import com.example.zakat.management.system.entities.BeneficiaryDonorId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BeneficiaryDonorRepository extends JpaRepository<BeneficiaryDonor, BeneficiaryDonorId> {

    @Query(value = "SELECT * FROM beneficiary_donor WHERE d_id = :dId", nativeQuery = true)
    List<BeneficiaryDonor> findByIdDId(@Param("dId") Long dId);

    @Query(value = "SELECT * FROM beneficiary_donor WHERE b_id = :bId", nativeQuery = true)
    List<BeneficiaryDonor> findByIdBId(@Param("bId") Long bId);

    @Query(value = "SELECT * FROM beneficiary_donor", nativeQuery = true)
    List<BeneficiaryDonor> findAll();
}