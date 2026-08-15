package com.example.zakat.management.system.mappers;

import com.example.zakat.management.system.dtos.response.InventoryResponse;
import com.example.zakat.management.system.entities.Inventory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InventoryMapper {
    @Mapping(target="donorId", expression = "java(inventory.getDonor().getId())")
    InventoryResponse toResponse(Inventory inventory);
}