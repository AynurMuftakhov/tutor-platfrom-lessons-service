package com.mytutorplatform.lessonsservice.mapper;

import com.mytutorplatform.lessonsservice.model.Lesson;
import com.mytutorplatform.lessonsservice.model.request.CreateLessonRequest;
import com.mytutorplatform.lessonsservice.model.request.UpdateLessonRequest;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, componentModel = "spring")
public interface LessonsMapper {
    void update(@MappingTarget Lesson existingLesson, UpdateLessonRequest updatedLesson);

    Lesson map(CreateLessonRequest createLessonRequest);
}
