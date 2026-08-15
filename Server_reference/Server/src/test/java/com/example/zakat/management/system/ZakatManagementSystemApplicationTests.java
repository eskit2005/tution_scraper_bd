package com.example.zakat.management.system;

import com.example.zakat.management.system.repositories.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ZakatManagementSystemApplicationTests {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DonorRepository donorRepository;

    @Autowired
    private BeneficiaryRepository beneficiaryRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private ReceiptRepository receiptRepository;

    @Autowired
    private ZakatAssignmentRepository zakatAssignmentRepository;

    @Test
    void contextLoads() {
        assertNotNull(userRepository);
    }

    @Test
    void testUserRepositoryQueries() {
        // Test polymorphic load with JPA derived query (Login fix)
        userRepository.findAll();
        userRepository.findByEmail("admin@example.com");
    }

    @Test
    void testInheritedRepositories() {
        // Test native JOIN queries
        donorRepository.findAll();
        adminRepository.findAll();
        beneficiaryRepository.findAll();
        beneficiaryRepository.findByEligibleTrueOrderByPriorityScoreDesc();
    }

    @Test
    void testSimpleRepositories() {
        // Test native SELECT * queries
        inventoryRepository.findAll();
        inventoryRepository.findByStatus("AVAILABLE");
        receiptRepository.findAll();
        receiptRepository.sumAllAmounts();
        zakatAssignmentRepository.findAll();
        zakatAssignmentRepository.sumAllAmounts();
    }
}
