package com.example.zakat.management.system.services;

import com.example.zakat.management.system.dtos.request.ZakatAssignmentRequest;
import com.example.zakat.management.system.dtos.response.ZakatAssignmentResponse;
import com.example.zakat.management.system.entities.Admin;
import com.example.zakat.management.system.entities.Beneficiary;
import com.example.zakat.management.system.entities.BeneficiaryAdminZakat;
import com.example.zakat.management.system.entities.Inventory;
import com.example.zakat.management.system.entities.Receipt;
import com.example.zakat.management.system.entities.ZakatAssignment;
import com.example.zakat.management.system.events.ZakatAssignedEvent;
import com.example.zakat.management.system.exceptions.IneligibleBeneficiaryException;
import com.example.zakat.management.system.exceptions.InsufficientFundsException;
import com.example.zakat.management.system.exceptions.ResourceNotFoundException;
import com.example.zakat.management.system.mappers.ZakatAssignmentMapper;
import com.example.zakat.management.system.repositories.AdminRepository;
import com.example.zakat.management.system.repositories.BeneficiaryAdminZakatRepository;
import com.example.zakat.management.system.repositories.BeneficiaryRepository;
import com.example.zakat.management.system.repositories.InventoryRepository;
import com.example.zakat.management.system.repositories.ReceiptRepository;
import com.example.zakat.management.system.repositories.UserRepository;
import com.example.zakat.management.system.repositories.ZakatAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ZakatAssignmentService {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final ZakatAssignmentRepository zakatAssignmentRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final ReceiptRepository receiptRepository;
    private final BeneficiaryAdminZakatRepository beneficiaryAdminZakatRepository;
    private final InventoryRepository inventoryRepository;
    private final UserRepository userRepository;
    private final AdminRepository adminRepository; // 1. INJECTED ADMIN REPOSITORY
    private final ZakatAssignmentMapper zakatAssignmentMapper;
    private final BeneficiaryService beneficiaryService;

    @Transactional
    public ZakatAssignmentResponse assign(ZakatAssignmentRequest request) {
        Beneficiary beneficiary = beneficiaryRepository.findById(request.getBeneficiaryId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Beneficiary not found with id: " + request.getBeneficiaryId()));

        if (beneficiary.getEligible() == null || !beneficiary.getEligible()) {
            throw new IneligibleBeneficiaryException("Beneficiary is not eligible for zakat");
        }

        if (request.getInventoryId() != null && request.getAmountAssigned() != null) {
            throw new IllegalArgumentException("Provide either money OR inventory, not both");
        }
        if (request.getInventoryId() == null && request.getAmountAssigned() == null) {
            throw new IllegalArgumentException("Provide either money OR inventory");
        }

        BigDecimal receivedAmount;
        ZakatAssignment assignment = new ZakatAssignment();

        if (request.getInventoryId() != null) {
            Inventory inventory = inventoryRepository.findById(request.getInventoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with id: " + request.getInventoryId()));
            if (inventory.getStatus() != null && !"AVAILABLE".equals(inventory.getStatus())) {
                throw new IllegalStateException("Inventory item is not available for assignment");
            }
            assignment.setInventory(inventory);
            receivedAmount = inventory.getAppoxValue();
            if (receivedAmount == null) {
                throw new IllegalArgumentException("Inventory item has no approximate value set");
            }
            inventory.setStatus("ASSIGNED");
            inventoryRepository.save(inventory);
        } else {
            BigDecimal totalDonated = BigDecimal.valueOf(receiptRepository.sumAllAmounts());
            BigDecimal totalAssigned = BigDecimal.valueOf(zakatAssignmentRepository.sumAllAmounts());

            BigDecimal remaining = totalDonated.subtract(totalAssigned);

            if (request.getAmountAssigned().compareTo(remaining) > 0) {
                throw new InsufficientFundsException(
                        "Insufficient funds. Requested: " + request.getAmountAssigned()
                                + ", Available: " + remaining);
            }
            receivedAmount = request.getAmountAssigned();
        }

        assignment.setAmountAssigned(receivedAmount);
        ZakatAssignment saved = zakatAssignmentRepository.save(assignment);

        BigDecimal currentTotal = beneficiary.getTotalReceivedValue() != null
                ? beneficiary.getTotalReceivedValue()
                : BigDecimal.ZERO;
        beneficiary.setTotalReceivedValue(currentTotal.add(receivedAmount));
        beneficiaryService.recalculatePriorityScore(beneficiary);
        beneficiaryRepository.save(beneficiary);

        Admin admin = adminRepository.findById(request.getAdminId())
                .orElseThrow(() -> new ResourceNotFoundException("Admin not found with id: " + request.getAdminId()));

        BeneficiaryAdminZakat baz = new BeneficiaryAdminZakat();

        baz.setAssignment(saved);
        baz.setBeneficiary(beneficiary);
        baz.setAdmin(admin);

        beneficiaryAdminZakatRepository.save(baz);

        var user = userRepository.findById(beneficiary.getId()).orElse(null);
        if (user != null) {
            applicationEventPublisher.publishEvent(new ZakatAssignedEvent(
                    user.getEmail(),
                    user.getName(),
                    saved.getAmountAssigned()
            ));
        }

        return zakatAssignmentMapper.toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<ZakatAssignmentResponse> getAllAssignments() {
        return zakatAssignmentRepository.findAll().stream()
                .map(zakatAssignmentMapper::toResponse)
                .collect(Collectors.toList());
    }
}