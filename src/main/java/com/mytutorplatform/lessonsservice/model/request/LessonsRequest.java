package com.mytutorplatform.lessonsservice.model.request;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class LessonsRequest {
    private String title;
    private OffsetDateTime dateTime;
    private Integer duration;
    private UUID studentId;
    private String location;
    private String lessonPlan;
    private String learningObjectives;
}
