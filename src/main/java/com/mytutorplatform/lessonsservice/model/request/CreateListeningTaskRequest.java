package com.mytutorplatform.lessonsservice.model.request;

import lombok.Data;

import java.util.UUID;

@Data
public class CreateListeningTaskRequest {
    private String title;
    private Integer startSec;
    private Integer endSec;
    private Integer wordLimit;
    private Integer timeLimitSec;
    private UUID materialId;
}
