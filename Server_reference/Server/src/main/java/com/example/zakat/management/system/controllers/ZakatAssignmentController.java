package com.example.zakat.management.system.controllers;

import com.example.zakat.management.system.dtos.request.ZakatAssignmentRequest;
import com.example.zakat.management.system.dtos.response.ZakatAssignmentResponse;
import com.example.zakat.management.system.services.ZakatAssignmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/assignments")
@RequiredArgsConstructor
public class ZakatAssignmentController {

    private final ZakatAssignmentService zakatAssignmentService;

    @PostMapping("")
    public ResponseEntity<ZakatAssignmentResponse> assign(
            @Valid @RequestBody ZakatAssignmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(zakatAssignmentService.assign(request));
    }

    @GetMapping("")
    public ResponseEntity<List<ZakatAssignmentResponse>> getAllAssignments() {
        return ResponseEntity.ok(zakatAssignmentService.getAllAssignments());
    }
}