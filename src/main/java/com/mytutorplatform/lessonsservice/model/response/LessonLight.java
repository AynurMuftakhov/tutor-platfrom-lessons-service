package com.mytutorplatform.lessonsservice.model.response;

import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
public class LessonLight {

    private UUID id;

    private OffsetDateTime dateTime;

    private OffsetDateTime endDate;

    private int duration;

    private UUID tutorId;
}
