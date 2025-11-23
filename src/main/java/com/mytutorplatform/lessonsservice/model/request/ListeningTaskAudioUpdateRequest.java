package com.mytutorplatform.lessonsservice.model.request;

import lombok.Data;

import java.util.UUID;

@Data
public class ListeningTaskAudioUpdateRequest {
    private UUID jobId; // optional
    private String audioUrl; // optional
    private ListeningVoiceConfigRequest voice; // optional
    private String language; // optional
}
