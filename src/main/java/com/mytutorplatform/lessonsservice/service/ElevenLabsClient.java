package com.mytutorplatform.lessonsservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mytutorplatform.lessonsservice.model.request.VoiceSettings;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class ElevenLabsClient {

    private final ObjectMapper objectMapper;

    @Value("${elevenlabs.baseUrl:https://api.elevenlabs.io}")
    private String baseUrl;
    @Value("${elevenlabs.apiKey:${ELEVENLABS_API_KEY:}}")
    private String apiKey;
    @Value("${elevenlabs.model:eleven_multilingual_v2}")
    private String defaultModel;
    @Value("${elevenlabs.outputFormat:mp3_44100_128}")
    private String defaultOutputFormat;
    @Value("${elevenlabs.timeoutSec:120}")
    private int timeoutSec;
    @Value("${elevenlabs.maxRetries:2}")
    private int maxRetries;
    @Value("${elevenlabs.retryBackoffMs:500}")
    private long retryBackoffMs;

    private volatile CachedVoicesCache voicesCache;
    private volatile HttpClient httpClient;

    private HttpClient client() {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("ElevenLabs API key is not configured");
        }
        if (httpClient == null) {
            httpClient = HttpClient.newBuilder()
                    .build();
        }
        return httpClient;
    }

    public byte[] createSpeech(String text,
                               String voiceId,
                               String modelId,
                               String languageCode,
                               VoiceSettings voiceSettings,
                               String outputFormat,
                               String requestId) {
        String model = (modelId == null || modelId.isBlank()) ? defaultModel : modelId;
        String fmt = (outputFormat == null || outputFormat.isBlank()) ? defaultOutputFormat : outputFormat;
        String url = baseUrl + "/v1/text-to-speech/" + voiceId + "?output_format=" + fmt;

        Map<String, Object> body = new HashMap<>();
        body.put("text", text);
        body.put("model_id", model);
        if (voiceSettings != null) {
            Map<String, Object> vs = new HashMap<>();
            if (voiceSettings.stability() != null) vs.put("stability", voiceSettings.stability());
            if (voiceSettings.similarity_boost() != null) vs.put("similarity_boost", voiceSettings.similarity_boost());
            if (voiceSettings.style() != null) vs.put("style", voiceSettings.style());
            if (voiceSettings.use_speaker_boost() != null) vs.put("use_speaker_boost", voiceSettings.use_speaker_boost());
            if (voiceSettings.speed() != null) vs.put("speed", voiceSettings.speed());
            if (!vs.isEmpty()) body.put("voice_settings", vs);
        }

        byte[] requestBytes;
        try {
            requestBytes = objectMapper.writeValueAsBytes(body);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to serialize ElevenLabs request body", e);
        }

        // Logging input (truncated text to avoid huge logs)
        String preview = text == null ? "" : (text.length() > 200 ? text.substring(0, 200) + "…" : text);
        log.info("[{}] ElevenLabs TTS POST {} model={} voice={} fmt={} bodyTextPreview='{}'", requestId, url, model, voiceId, fmt, preview);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(timeoutSec))
                .header("xi-api-key", apiKey)
                .header("Content-Type", MediaType.APPLICATION_JSON_VALUE)
                .POST(HttpRequest.BodyPublishers.ofByteArray(requestBytes))
                .build();

        try {
            HttpResponse<byte[]> response = client().send(request, HttpResponse.BodyHandlers.ofByteArray());
            int status = response.statusCode();
            String vendorReqId = firstHeader(response.headers(), "x-request-id");
            if (vendorReqId != null) {
                log.debug("[{}] ElevenLabs x-request-id: {}", requestId, vendorReqId);
            }
            if (status >= 200 && status < 300) {
                byte[] bytes = response.body();
                if (bytes == null || bytes.length == 0) throw new IllegalStateException("Empty response from ElevenLabs");
                return bytes;
            }

            String errText = safeBodyAsText(response.body());
            log.warn("[{}] ElevenLabs TTS error status={} body={} headers={}", requestId, status, errText, response.headers().map());

            throw new IllegalStateException("ElevenLabs TTS failed with status=" + status + " body=" + errText);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("ElevenLabs TTS request interrupted", ie);
        } catch (Exception e) {
            log.warn("[{}] ElevenLabs TTS call failed: {}", requestId, e.toString());
             throw new IllegalStateException("ElevenLabs TTS request failed after retries", e);
        }
    }

    public synchronized List<VoiceInfo> listVoices() {
        if (voicesCache != null && !voicesCache.isExpired()) {
            return voicesCache.voices;
        }
        String url = baseUrl + "/v2/voices";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(Math.min(timeoutSec, 20)))
                .header("xi-api-key", apiKey)
                .GET()
                .build();

        int attempts = Math.max(1, maxRetries + 1);
        for (int i = 1; i <= attempts; i++) {
            try {
                HttpResponse<String> response = client().send(request, HttpResponse.BodyHandlers.ofString());
                int status = response.statusCode();
                if (status >= 200 && status < 300) {
                    List<VoiceInfo> voices = parseVoices(response.body());
                    this.voicesCache = new CachedVoicesCache(voices, System.currentTimeMillis());
                    return voices;
                }
                String errText = response.body();
                log.warn("ElevenLabs voices listing failed status={} body={}", status, errText);
                if (status == 429 || (status >= 500 && status <= 599)) {
                    backoff(i);
                    continue;
                }
                return List.of();
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return List.of();
            } catch (Exception e) {
                log.warn("ElevenLabs voices call attempt {}/{} failed: {}", i, attempts, e.toString());
                if (i == attempts) return List.of();
                backoff(i);
            }
        }
        return List.of();
    }

    private void backoff(int attempt) {
        long sleep = Math.max(0, retryBackoffMs) * attempt;
        try { Thread.sleep(sleep); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
    }

    private String firstHeader(HttpHeaders headers, String name) {
        try {
            return headers.firstValue(name).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    private String safeBodyAsText(byte[] bytes) {
        if (bytes == null) return "";
        try {
            return new String(bytes);
        } catch (Exception e) {
            return "<binary:" + bytes.length + ">";
        }
    }

    private List<VoiceInfo> parseVoices(String json) {
        // Very naive JSON parsing to avoid adding libs: look for patterns
        // Expected structure: { "voices": [ {"voice_id":"...","name":"...","preview_url":"..." ...}, ... ] }
        if (json == null || json.isBlank()) return List.of();
        List<VoiceInfo> result = new ArrayList<>();
        try {
            int idx = json.indexOf("\"voices\"");
            if (idx < 0) return List.of();
            int arrStart = json.indexOf('[', idx);
            int arrEnd = json.indexOf(']', arrStart);
            if (arrStart < 0 || arrEnd < 0) return List.of();
            String arr = json.substring(arrStart + 1, arrEnd);
            String[] items = arr.split("\\},\\s*\\{");
            for (String raw : items) {
                String block = raw;
                if (!block.startsWith("{")) block = "{" + block;
                if (!block.endsWith("}")) block = block + "}";
                String voiceId = extract(block, "voice_id");
                String name = extract(block, "name");
                String preview = extract(block, "preview_url");
                result.add(new VoiceInfo(voiceId, name, preview, Map.of()));
            }
        } catch (Exception e) {
            log.warn("Failed to parse voices: {}", e.getMessage());
        }
        return result;
    }

    private String extract(String json, String key) {
        int idx = json.indexOf("\"" + key + "\"");
        if (idx < 0) return null;
        int colon = json.indexOf(':', idx);
        int q1 = json.indexOf('"', colon + 1);
        int q2 = json.indexOf('"', q1 + 1);
        if (q1 < 0 || q2 < 0) return null;
        return json.substring(q1 + 1, q2);
    }

    @Builder
    public record VoiceInfo(String voiceId, String name, String previewUrl, Map<String, Object> settings) {}

    private static class CachedVoicesCache {
        final List<VoiceInfo> voices;
        final long fetchedAtMs;
        CachedVoicesCache(List<VoiceInfo> voices, long fetchedAtMs) {
            this.voices = voices;
            this.fetchedAtMs = fetchedAtMs;
        }
        boolean isExpired() {
            long ttlMs = 24L * 60 * 60 * 1000; // 24h
            return System.currentTimeMillis() - fetchedAtMs > ttlMs;
        }
    }
}
