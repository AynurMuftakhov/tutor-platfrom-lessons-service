package com.mytutorplatform.lessonsservice.model.response;

import com.mytutorplatform.lessonsservice.model.ListeningTaskStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
public class ListeningTaskDTO {
    private UUID id;
    private String title;
    private Integer startSec;
    private Integer endSec;
    private Integer wordLimit;
    private Integer timeLimitSec;
    private UUID materialId;
    private UUID transcriptId;
    private String transcriptText;
    private List<String> targetWords;
    private String audioUrl;
    private ListeningVoiceConfigDTO voice;
    private String language;
    private String difficulty;
    private String notes;
    private ListeningTaskStatus status;
    private LocalDateTime createdAt;
}
