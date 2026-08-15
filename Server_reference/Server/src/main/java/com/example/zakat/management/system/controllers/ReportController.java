package com.example.zakat.management.system.controllers;

import com.example.zakat.management.system.dtos.response.ReportResponse;
import com.example.zakat.management.system.services.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping
    public ResponseEntity<ReportResponse> getReport() {
        return ResponseEntity.ok(reportService.getReport());
    }
}
