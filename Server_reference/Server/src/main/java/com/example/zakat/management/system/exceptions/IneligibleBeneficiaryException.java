package com.example.zakat.management.system.exceptions;

public class IneligibleBeneficiaryException extends RuntimeException {
    public IneligibleBeneficiaryException(String message) {
        super(message);
    }
}
