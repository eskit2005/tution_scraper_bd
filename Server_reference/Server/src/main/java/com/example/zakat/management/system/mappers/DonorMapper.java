package com.example.zakat.management.system.mappers;

import com.example.zakat.management.system.dtos.response.DonorResponse;
import com.example.zakat.management.system.entities.Donor;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DonorMapper {
    DonorResponse toResponse(Donor donor);
}