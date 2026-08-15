package com.example.zakat.management.system.services;

import com.example.zakat.management.system.dtos.request.InventoryRequest;
import com.example.zakat.management.system.dtos.request.ItemDonationRequest;
import com.example.zakat.management.system.dtos.response.InventoryResponse;
import com.example.zakat.management.system.entities.Donor;
import com.example.zakat.management.system.entities.Inventory;
import com.example.zakat.management.system.exceptions.ResourceNotFoundException;
import com.example.zakat.management.system.mappers.InventoryMapper;
import com.example.zakat.management.system.repositories.DonorRepository;
import com.example.zakat.management.system.repositories.InventoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InventoryService {

    private final InventoryRepository inventoryRepository;
    private final DonorRepository donorRepository;
    private final InventoryMapper inventoryMapper;

    @Transactional
    public InventoryResponse addItem(InventoryRequest request) {
        Inventory inventory = new Inventory();
        inventory.setName(request.getName());
        inventory.setAppoxValue(request.getApproxValue());
        inventory.setStatus("AVAILABLE");
        inventory.setReceivedAt(Instant.now());
        Donor donor = donorRepository.findById(request.getDonorId())
                .orElseThrow(() -> new ResourceNotFoundException("Donor not found with id: " + request.getDonorId()));
        inventory.setDonor(donor);


        return inventoryMapper.toResponse(inventoryRepository.save(inventory));
    }



    @Transactional(readOnly = true)
    public List<InventoryResponse> getAllItems() {
        return inventoryRepository.findAll().stream()
                .map(inventoryMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<InventoryResponse> getAvailableItems() {
        return inventoryRepository.findByStatus("AVAILABLE").stream()
                .map(inventoryMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public InventoryResponse getById(Long id) {
        return inventoryMapper.toResponse(
                inventoryRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Item not found with id: " + id))
        );
    }
}