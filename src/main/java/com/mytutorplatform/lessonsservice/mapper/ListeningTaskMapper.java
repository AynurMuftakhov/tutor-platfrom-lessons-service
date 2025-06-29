package com.mytutorplatform.lessonsservice.mapper;

import com.mytutorplatform.lessonsservice.model.Lesson;
import com.mytutorplatform.lessonsservice.model.ListeningTask;
import com.mytutorplatform.lessonsservice.model.request.CreateLessonRequest;
import com.mytutorplatform.lessonsservice.model.request.CreateListeningTaskRequest;
import com.mytutorplatform.lessonsservice.model.request.UpdateLessonRequest;
import com.mytutorplatform.lessonsservice.model.response.LessonLight;
import com.mytutorplatform.lessonsservice.model.response.ListeningTaskDTO;
import org.mapstruct.*;

import java.util.List;

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE , componentModel = "spring")
public interface ListeningTaskMapper {
    void update(@MappingTarget ListeningTask existingLesson, CreateListeningTaskRequest updatedLesson);

    ListeningTask map(CreateListeningTaskRequest createLessonRequest);

    ListeningTaskDTO map(ListeningTask listeningTask);

    List<ListeningTaskDTO> mapList(List<ListeningTask> listeningTasks);
}