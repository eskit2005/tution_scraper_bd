package com.example.zakat.management.system.mappers;

import com.example.zakat.management.system.dtos.response.BeneficiaryResponse;
import com.example.zakat.management.system.entities.Beneficiary;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BeneficiaryMapper {

    @Mapping(source = "id", target = "userId")
    @Mapping(source = "name", target = "fullName")
    BeneficiaryResponse toResponse(Beneficiary beneficiary);
}