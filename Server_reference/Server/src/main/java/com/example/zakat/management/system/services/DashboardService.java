package com.example.zakat.management.system.services;

import com.example.zakat.management.system.dtos.response.DashboardResponse;
import com.example.zakat.management.system.repositories.BeneficiaryRepository;
import com.example.zakat.management.system.repositories.ReceiptRepository;
import com.example.zakat.management.system.repositories.UserRepository;
import com.example.zakat.management.system.repositories.ZakatAssignmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final UserRepository userRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final ReceiptRepository receiptRepository;
    private final ZakatAssignmentRepository zakatAssignmentRepository;

    public DashboardResponse getDashboard() {
        Double totalDonated = receiptRepository.sumAllAmounts();
        Double totalDistributed = zakatAssignmentRepository.sumAllAmounts();
        Double remaining = (totalDonated != null ? totalDonated : 0.0) - (totalDistributed != null ? totalDistributed : 0.0);

        DashboardResponse dashboard = new DashboardResponse();
        dashboard.setTotalDonors(userRepository.countByRole("DONOR"));
        dashboard.setTotalBeneficiaries(beneficiaryRepository.count());
        dashboard.setTotalDonated(totalDonated != null ? totalDonated : 0.0);
        dashboard.setTotalDistributed(totalDistributed != null ? totalDistributed : 0.0);
        dashboard.setRemaining(remaining);
        return dashboard;
    }
}