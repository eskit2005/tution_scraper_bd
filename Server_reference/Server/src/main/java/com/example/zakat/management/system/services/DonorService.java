package com.example.zakat.management.system.services;

import com.example.zakat.management.system.dtos.request.DonationRequest;
import com.example.zakat.management.system.dtos.response.DonationResponse;
import com.example.zakat.management.system.dtos.response.DonorResponse;
import com.example.zakat.management.system.entities.Donor;
import com.example.zakat.management.system.entities.Receipt;
import com.example.zakat.management.system.exceptions.ResourceNotFoundException;
import com.example.zakat.management.system.mappers.DonorMapper;
import com.example.zakat.management.system.repositories.DonorRepository;
import com.example.zakat.management.system.repositories.ReceiptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class DonorService {

    private final DonorRepository donorRepository;
    private final ReceiptRepository receiptRepository;
    private final DonorMapper donorMapper;

    @Transactional
    public DonationResponse createDonationReceipt(DonationRequest request) {
        Donor donor = donorRepository.findById(request.getDonorId())
                .orElseThrow(() -> new ResourceNotFoundException("Donor not found with id: " + request.getDonorId()));

        Receipt receipt = new Receipt();
        receipt.setRecepNum(generateRecepNum());
        receipt.setAmount(request.getAmount());
        receipt.setDonor(donor);

        Receipt saved = receiptRepository.save(receipt);

        // Fetch fresh to get auto-generated ID and DB-set timestamp
        Receipt fresh = receiptRepository.findByRecepNum(saved.getRecepNum())
                .orElse(saved);

        DonationResponse response = new DonationResponse();
        response.setReceiptId(fresh.getReceiptId().getId());
        response.setRecepNum(fresh.getRecepNum());
        response.setDonorName(donor.getName());
        response.setAmount(fresh.getAmount());
        response.setDescription(request.getDescription());
        response.setIssuedAt(fresh.getIssuedAt());

        return response;
    }

    private String generateRecepNum() {
        LocalDate now = LocalDate.now();
        String yyMM = now.format(DateTimeFormatter.ofPattern("yyMM"));

        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        Random random = new Random();
        StringBuilder hash = new StringBuilder();
        for (int i = 0; i < 5; i++) {
            hash.append(chars.charAt(random.nextInt(chars.length())));
        }

        return "ZKT-" + yyMM + "-" + hash.toString();
    }

    @Transactional(readOnly = true)
    public DonorResponse getDonorById(Long id) {
        Donor donor = donorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Donor not found with id: " + id));
        return donorMapper.toResponse(donor);
    }

    @Transactional(readOnly = true)
    public Double getTotalDonationsByDonorId(Long donorId) {
        if (!donorRepository.existsById(donorId)) {
            throw new ResourceNotFoundException("Donor not found with id: " + donorId);
        }
        return receiptRepository.sumAmountsByDonorId(donorId);
    }
}