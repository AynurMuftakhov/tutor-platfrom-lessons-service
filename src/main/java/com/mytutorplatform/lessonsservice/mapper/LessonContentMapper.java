package com.mytutorplatform.lessonsservice.mapper;

import com.mytutorplatform.lessonsservice.model.LessonContent;
import com.mytutorplatform.lessonsservice.model.response.LessonContentDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        componentModel = "spring")
public interface LessonContentMapper {

    LessonContentDto toDto(LessonContent entity);

    @Mapping(target = "id", ignore = true)
    LessonContent toEntity(LessonContentDto dto);
}
