package com.tuition.app.dto;

import com.tuition.app.entity.TuitionPost;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TuitionMapper {

    TuitionPostDto toDto(TuitionPost post);
    
    TuitionPost toEntity(TuitionPostDto dto);

    List<TuitionPostDto> toDtoList(List<TuitionPost> posts);
}
