package com.example.zakat.management.system.mappers;

import com.example.zakat.management.system.dtos.response.ZakatAssignmentResponse;
import com.example.zakat.management.system.entities.ZakatAssignment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ZakatAssignmentMapper {

    ZakatAssignmentResponse toResponse(ZakatAssignment assignment);
}