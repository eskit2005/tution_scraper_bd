package com.example.zakat.management.system.mappers;

import com.example.zakat.management.system.dtos.response.UserResponse;
import com.example.zakat.management.system.entities.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);
}