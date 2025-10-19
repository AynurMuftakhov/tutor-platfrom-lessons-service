package com.mytutorplatform.lessonsservice.model.request;

import java.util.Map;
import java.util.UUID;

public record AudioGenerateRequest(
        UUID transcriptId,
        String transcriptOverride,
        String voiceId,
        String ttsModel,
        String languageCode,
        VoiceSettings voiceSettings,
        String outputFormat,
        Map<String, Object> metadata
) {}
