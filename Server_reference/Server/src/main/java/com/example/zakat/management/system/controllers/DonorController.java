package com.example.zakat.management.system.controllers;

import com.example.zakat.management.system.dtos.request.DonationRequest;
import com.example.zakat.management.system.dtos.request.DirectDonationRequest;
import com.example.zakat.management.system.dtos.response.DonationResponse;
import com.example.zakat.management.system.dtos.response.BeneficiaryResponse;
import com.example.zakat.management.system.services.DonorService;
import com.example.zakat.management.system.services.DirectDonationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/donors")
@RequiredArgsConstructor
public class DonorController {

    private final DonorService donorService;
    private final DirectDonationService directDonationService;

    @PostMapping("")
    public ResponseEntity<DonationResponse> donateMoney(@Valid @RequestBody DonationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(donorService.createDonationReceipt(request));
    }

    @PostMapping("/{id}/beneficiaries")
    public ResponseEntity<Void> donateToBeneficiary(
            @PathVariable Long id,
            @Valid @RequestBody DirectDonationRequest request) {
        directDonationService.donateToBeneficiary(request, id);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getDonorById(@PathVariable Long id) {
        return ResponseEntity.ok(donorService.getDonorById(id));
    }

    @GetMapping("/{id}/beneficiaries")
    public ResponseEntity<List<BeneficiaryResponse>> getDonatedBeneficiaries(@PathVariable Long id) {
        return ResponseEntity.ok(directDonationService.getBeneficiariesDonatedTo(id));
    }

    @GetMapping("/{id}/total-donations")
    public ResponseEntity<Double> getTotalDonations(@PathVariable Long id) {
        return ResponseEntity.ok(donorService.getTotalDonationsByDonorId(id));
    }
}
