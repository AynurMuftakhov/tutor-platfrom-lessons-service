package com.mytutorplatform.lessonsservice.model.kafka;

import lombok.Data;
import lombok.ToString;

import java.time.OffsetDateTime;
import java.util.Date;
import java.util.UUID;

@Data
@ToString
public class Event {
    private String eventType;
    private Date timestamp;
    private UUID lessonId;
    private UUID tutorId;
    private UUID[] studentIds;
    private OffsetDateTime startTime;
    private OffsetDateTime oldStartTime;
}
