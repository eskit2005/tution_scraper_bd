package com.example.zakat.management.system.mappers;

import com.example.zakat.management.system.dtos.response.ReceiptResponse;
import com.example.zakat.management.system.entities.Receipt;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReceiptMapper {

    @Mapping(source = "receiptId.id", target = "id")
    @Mapping(source = "receiptId.DId", target = "donorId")
    @Mapping(target = "donorName", expression = "java(receipt.getDonor().getName())")
    ReceiptResponse toResponse(Receipt receipt);
}