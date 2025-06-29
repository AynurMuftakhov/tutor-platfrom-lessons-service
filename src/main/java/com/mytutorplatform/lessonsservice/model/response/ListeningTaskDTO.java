package com.mytutorplatform.lessonsservice.model.response;

import lombok.Data;

import java.time.LocalDateTime;
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
    private LocalDateTime createdAt;
}
