package com.mytutorplatform.lessonsservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class OpenAiClient {

    private final WebClient.Builder webClientBuilder;
    private final ObjectMapper objectMapper;

    @Value("${openai.api.url:https://api.openai.com/v1}")
    private String url;

    @Value("${openai.apiKey:${openai.api.key:}}")
    private String apiKey;

    @Value("${openai.model:gpt-4.1-mini}")
    private String model;

    @Value("${openai.timeoutSec:20}")
    private int timeoutSec;

    @Value("${openai.maxRetries:2}")
    private int maxRetries;

    @Value("${openai.retryBackoffMs:300}")
    private long retryBackoffMs;

    public String generateTranscript(TranscriptPromptPayload payload, String requestId) {
        List<Map<String, Object>> messages = List.of(
                Map.of("role", "system", "content", payload.systemMessage()),
                Map.of("role", "user", "content", payload.userMessage())
        );
        return generateText(messages, 0.7, null, payload.seed(), requestId);
    }

    public String generateText(List<Map<String, Object>> messages,
                               Double temperature,
                               Integer maxTokens,
                               Integer seed,
                               String requestId) {
        return callOpenAi(messages, temperature, maxTokens, seed, requestId);
    }

    private String callOpenAi(List<Map<String, Object>> messages,
                              Double temperature,
                              Integer maxTokens,
                              Integer seed,
                              String requestId) {
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OpenAI API key is not configured");
        }

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", messages);
        body.put("temperature", temperature != null ? temperature : 0.7);
        if (maxTokens != null) {
            body.put("max_tokens", maxTokens);
        }
        if (seed != null) {
            body.put("seed", seed);
        }

        WebClient client = webClientBuilder
                .baseUrl(url)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();

        Mono<String> call = client.post()
                .body(BodyInserters.fromValue(body))
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(timeoutSec))
                .retryWhen(reactor.util.retry.Retry.backoff(maxRetries, Duration.ofMillis(retryBackoffMs)))
                .doOnError(err -> log.error("OpenAI request failed [{}]", requestId, err));

        String json = call.block(Duration.ofSeconds(timeoutSec + 2L));
        if (json == null || json.isBlank()) {
            throw new IllegalStateException("Empty response from OpenAI");
        }

        String content = extractContent(json);
        if (content != null && !content.isBlank()) {
            return content;
        }

        log.warn("OpenAI response missing content, returning raw JSON [{}]", requestId);
        return json; // let service decide
    }

    private String extractContent(String json) {
        try {
            JsonNode root = objectMapper.readTree(json);
            JsonNode choices = root.path("choices");
            if (choices.isArray() && !choices.isEmpty()) {
                JsonNode message = choices.get(0).path("message");
                JsonNode content = message.path("content");
                if (!content.isMissingNode()) {
                    return content.asText();
                }
            }
        } catch (Exception e) {
            log.warn("Failed to parse OpenAI response: {}", e.getMessage());
        }
        return null;
    }

    @Builder
    public record TranscriptPromptPayload(
            String systemMessage,
            String userMessage,
            Integer seed
    ) {}
}
