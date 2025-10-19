package com.mytutorplatform.lessonsservice.model.request;

public record VoiceSettings(
        Double stability,
        Double similarity_boost,
        Double style,
        Boolean use_speaker_boost,
        Double speed
) {}
