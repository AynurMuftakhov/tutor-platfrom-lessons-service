package com.mytutorplatform.lessonsservice.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class VocabularyClient {

    private final RestTemplate restTemplate;

    @Value("${vocabulary.baseUrl:http://localhost:8085}")
    private String baseUrl;

    @Value("${vocabulary.wordsPath:/api/vocabulary/words}")
    private String wordsPath;

    public List<VocabWord> getWordsByIds(List<UUID> ids) {
        if (CollectionUtils.isEmpty(ids)) return List.of();
        String joined = ids.stream().map(String::valueOf).collect(Collectors.joining(","));
        String url = baseUrl + wordsPath + "?ids=" + joined;
        try {
            ResponseEntity<VocabWord[]> response = restTemplate.getForEntity(URI.create(url), VocabWord[].class);
            VocabWord[] body = response.getBody();
            if (body == null) return List.of();
            // deduplicate by id and validate no duplicates from service
            Map<UUID, VocabWord> byId = new LinkedHashMap<>();
            for (VocabWord v : body) {
                if (byId.containsKey(v.getId())) {
                    throw new IllegalArgumentException("Duplicate vocabulary IDs returned by vocabulary-service");
                }
                byId.put(v.getId(), v);
            }
            if (byId.isEmpty()) return List.of();
            return new ArrayList<>(byId.values());
        } catch (Exception e) {
            log.error("Failed to resolve vocabulary words: {}", e.getMessage(), e);
            throw new IllegalArgumentException("Unable to resolve vocabulary words");
        }
    }

    @Data
    @AllArgsConstructor
    public static class VocabWord {
        private UUID id;
        private String text;
        private String language;

        public VocabWord() {}
    }
}
