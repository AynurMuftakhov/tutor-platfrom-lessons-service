package com.mytutorplatform.lessonsservice.model.response;

import lombok.Data;

@Data
public class ListeningVoiceConfigDTO {
    private String id;
    private Double speed;
    private Double pitch;
    private String style;
}
