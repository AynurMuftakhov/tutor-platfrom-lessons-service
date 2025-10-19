package com.mytutorplatform.lessonsservice.service;

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
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("OpenAI API key is not configured");
        }

        Map<String, Object> body = new HashMap<>();
        body.put("model", model);
        body.put("messages", List.of(
                Map.of("role", "system", "content", payload.systemMessage()),
                Map.of("role", "user", "content", payload.userMessage())
        ));
        body.put("temperature", 0.7);
        if (payload.seed() != null) {
            body.put("seed", payload.seed());
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

        // naive JSON parse to extract first message content
        try {
            // Avoid adding extra deps: simple string extraction
            int idxChoices = json.indexOf("\"choices\"");
            if (idxChoices < 0) throw new IllegalStateException("Invalid OpenAI response: no choices");
            int idxContent = json.indexOf("\"content\"", idxChoices);
            if (idxContent < 0) throw new IllegalStateException("Invalid OpenAI response: no content");
            int idxColon = json.indexOf(":", idxContent);
            int idxQuoteStart = json.indexOf('"', idxColon + 1);
            int idxQuoteEnd = json.indexOf('"', idxQuoteStart + 1);
            if (idxQuoteStart < 0 || idxQuoteEnd < 0) throw new IllegalStateException("Invalid OpenAI response content");
            return json.substring(idxQuoteStart + 1, idxQuoteEnd);
        } catch (Exception e) {
            log.warn("Fallback to full JSON when parsing content failed: {}", e.getMessage());
            return json; // let service decide
        }
    }

    @Builder
    public record TranscriptPromptPayload(
            String systemMessage,
            String userMessage,
            Integer seed
    ) {}
}
