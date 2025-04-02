package com.mytutorplatform.lessonsservice.model.request;

import com.mytutorplatform.lessonsservice.model.LessonAttachment;
import com.mytutorplatform.lessonsservice.model.LessonSatisfaction;
import com.mytutorplatform.lessonsservice.model.LessonStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class UpdateLessonRequest extends LessonsRequest {
    private LessonStatus status;
    private String notes;
    private String studentPerformance;
    private LessonSatisfaction lessonSatisfaction;
    private List<LessonAttachment> attachments;
    private String homework;
}
