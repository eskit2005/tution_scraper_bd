package com.example.zakat.management.system.services;

import com.example.zakat.management.system.dtos.response.ReceiptResponse;
import com.example.zakat.management.system.exceptions.ResourceNotFoundException;
import com.example.zakat.management.system.mappers.ReceiptMapper;
import com.example.zakat.management.system.repositories.ReceiptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReceiptService {

    private final ReceiptRepository receiptRepository;
    private final ReceiptMapper receiptMapper;

    @Transactional(readOnly = true)
    public List<ReceiptResponse> getAllReceipts() {
        return receiptRepository.findAll().stream()
                .map(receiptMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReceiptResponse> getReceiptsByDonorId(Long donorId) {
        return receiptRepository.findAllByDonorIdDirectly(donorId).stream()
                .map(receiptMapper::toResponse)
                .toList();
    }
}