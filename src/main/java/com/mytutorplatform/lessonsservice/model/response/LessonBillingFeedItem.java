package com.mytutorplatform.lessonsservice.model.response;

import com.mytutorplatform.lessonsservice.model.LessonStatus;
import lombok.Builder;
import lombok.Data;

import java.time.OffsetDateTime;
import java.util.UUID;

@Data
@Builder
public class LessonBillingFeedItem {
    private UUID lessonId;
    private UUID tutorId;
    private UUID studentId;
    private LessonStatus status;
    private OffsetDateTime startTime;
    private Integer durationMinutes;
    private OffsetDateTime completedAt;
    private java.math.BigDecimal priceOverride;
    private java.math.BigDecimal quantity;
    private String currency;
}
