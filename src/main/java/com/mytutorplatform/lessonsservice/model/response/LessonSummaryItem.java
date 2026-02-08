package com.mytutorplatform.lessonsservice.model.response;

import com.mytutorplatform.lessonsservice.model.LessonStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class LessonSummaryItem {
    private UUID id;
    private Instant startsAtUtc;
    private Instant endsAtUtc;
    private LessonStatus status;
    private String title;
    private UUID studentId;
    private UUID tutorId;
    private String studentName;
    private String tutorName;
}
