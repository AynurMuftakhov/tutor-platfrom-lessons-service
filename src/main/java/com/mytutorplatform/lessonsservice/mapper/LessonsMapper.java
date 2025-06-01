package com.mytutorplatform.lessonsservice.mapper;

import com.mytutorplatform.lessonsservice.model.Lesson;
import com.mytutorplatform.lessonsservice.model.request.CreateLessonRequest;
import com.mytutorplatform.lessonsservice.model.request.UpdateLessonRequest;
import org.mapstruct.*;

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE , componentModel = "spring")
public interface LessonsMapper {
    void update(@MappingTarget Lesson existingLesson, UpdateLessonRequest updatedLesson);

    @Mapping(target = "endDate", expression = "java(createLessonRequest.getDateTime().plusMinutes(createLessonRequest.getDuration()))")
    Lesson map(CreateLessonRequest createLessonRequest);

    @AfterMapping
    default void setEndDate(@MappingTarget Lesson lesson, UpdateLessonRequest updatedLesson) {
        if (updatedLesson.getDateTime() != null && updatedLesson.getDuration() != null) {
            lesson.setEndDate(updatedLesson.getDateTime().plusMinutes(updatedLesson.getDuration()));
        }

        if (updatedLesson.getDateTime() != null && updatedLesson.getDuration() == null) {
            lesson.setEndDate(updatedLesson.getDateTime().plusMinutes(lesson.getDuration()));
        }

        if (updatedLesson.getDateTime() == null && updatedLesson.getDuration() != null) {
            lesson.setEndDate(lesson.getDateTime().plusMinutes(updatedLesson.getDuration()));
        }
    }
}