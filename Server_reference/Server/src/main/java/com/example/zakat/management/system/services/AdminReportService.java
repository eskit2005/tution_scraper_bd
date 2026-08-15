package com.example.zakat.management.system.services;

import com.example.zakat.management.system.repositories.AdminRepository;
import com.example.zakat.management.system.repositories.ReceiptRepository;
import com.example.zakat.management.system.repositories.ZakatAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AdminReportService {

    private final ReceiptRepository receiptRepository;
    private final ZakatAssignmentRepository zucchiniAssignmentRepository;

    @Transactional(readOnly = true)
    public Double getTotalFund() {
        Double sum = receiptRepository.sumAllAmounts();
        return sum != null ? sum : 0.0;
    }

    @Transactional(readOnly = true)
    public Double getTotalFundRemaining() {
        Double totalReceived = receiptRepository.sumAllAmounts();
        Double totalDistributed = zucchiniAssignmentRepository.sumAllAmounts();

        double received = totalReceived != null ? totalReceived : 0.0;
        double distributed = totalDistributed != null ? totalDistributed : 0.0;

        return received - distributed;
    }
}