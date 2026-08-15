package com.example.zakat.management.system.controllers;

import com.example.zakat.management.system.dtos.request.InventoryRequest;
import com.example.zakat.management.system.dtos.response.InventoryResponse;
import com.example.zakat.management.system.services.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @PostMapping("")
    public ResponseEntity<InventoryResponse> addItem(@Valid @RequestBody InventoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(inventoryService.addItem(request));
    }

    @GetMapping("")
    public ResponseEntity<List<InventoryResponse>> getAllItems() {
        return ResponseEntity.ok(inventoryService.getAllItems());
    }

    @GetMapping("/available")
    public ResponseEntity<List<InventoryResponse>> getAvailableItems() {
        return ResponseEntity.ok(inventoryService.getAvailableItems());
    }

    @GetMapping("/{id}")
    public ResponseEntity<InventoryResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(inventoryService.getById(id));
    }
}