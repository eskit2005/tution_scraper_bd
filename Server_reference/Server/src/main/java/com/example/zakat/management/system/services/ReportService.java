package com.example.zakat.management.system.services;

import com.example.zakat.management.system.dtos.response.ReportResponse;
import com.example.zakat.management.system.repositories.BeneficiaryRepository;
import com.example.zakat.management.system.repositories.ReceiptRepository;
import com.example.zakat.management.system.repositories.UserRepository;
import com.example.zakat.management.system.repositories.ZakatAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final ReceiptRepository receiptRepository;
    private final ZakatAssignmentRepository zakatAssignmentRepository;
    private final UserRepository userRepository;
    private final BeneficiaryRepository beneficiaryRepository;

    public ReportResponse getReport() {
        Double totalDonated = receiptRepository.sumAllAmounts();
        Double totalDistributed = zakatAssignmentRepository.sumAllAmounts();
        Double remaining = (totalDonated != null ? totalDonated : 0.0) - (totalDistributed != null ? totalDistributed : 0.0);

        ReportResponse report = new ReportResponse();
        report.setTotalDonated(totalDonated != null ? totalDonated : 0.0);
        report.setTotalDistributed(totalDistributed != null ? totalDistributed : 0.0);
        report.setRemaining(remaining);
        report.setTotalDonors(userRepository.countByRole("DONOR"));
        report.setTotalBeneficiaries(beneficiaryRepository.count());
        report.setTotalAssignments(zakatAssignmentRepository.count());
        return report;
    }
}