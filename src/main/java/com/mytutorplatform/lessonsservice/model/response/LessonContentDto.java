package com.mytutorplatform.lessonsservice.model.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.mytutorplatform.lessonsservice.model.LessonContentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LessonContentDto {
    private UUID id;
    private UUID ownerId;
    private String title;
    private LessonContentStatus status;
    private String[] tags;
    private String coverImageUrl;
    private JsonNode layout;
    private JsonNode content;
    private UUID createdBy;
    private Instant createdAt;
    private Instant updatedAt;
}