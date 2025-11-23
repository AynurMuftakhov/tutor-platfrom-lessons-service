package com.mytutorplatform.lessonsservice.model.request;

import lombok.Data;

@Data
public class ListeningVoiceConfigRequest {
    private String id;
    private Double speed;
    private Double pitch;
    private String style;
}
