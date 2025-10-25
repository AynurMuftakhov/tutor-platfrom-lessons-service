package com.mytutorplatform.lessonsservice.mapper;

import com.mytutorplatform.lessonsservice.model.ListeningTask;
import com.mytutorplatform.lessonsservice.model.request.CreateListeningTaskRequest;
import com.mytutorplatform.lessonsservice.model.response.ListeningTaskDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.List;

@Mapper(
        componentModel = "spring",
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface ListeningTaskMapper {

    void update(@MappingTarget ListeningTask existingLesson, CreateListeningTaskRequest updatedLesson);

    ListeningTask map(CreateListeningTaskRequest createLessonRequest);

    ListeningTaskDTO map(ListeningTask listeningTask);

    List<ListeningTaskDTO> mapList(List<ListeningTask> listeningTasks);
}
