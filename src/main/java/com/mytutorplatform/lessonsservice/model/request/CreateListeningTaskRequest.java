package com.mytutorplatform.lessonsservice.model.request;

import com.mytutorplatform.lessonsservice.model.ListeningTask.AssetType;
import lombok.Data;

@Data
public class CreateListeningTaskRequest {
    private AssetType assetType;
    private String sourceUrl;
    private Integer startSec;
    private Integer endSec;
    private Integer wordLimit;
    private Integer timeLimitSec;
}