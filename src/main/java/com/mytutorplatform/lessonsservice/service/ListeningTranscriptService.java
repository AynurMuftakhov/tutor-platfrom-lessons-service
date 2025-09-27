package com.mytutorplatform.lessonsservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mytutorplatform.lessonsservice.model.ListeningTranscript;
import com.mytutorplatform.lessonsservice.model.request.TranscriptGenerateRequest;
import com.mytutorplatform.lessonsservice.model.request.TranscriptUpdateRequest;
import com.mytutorplatform.lessonsservice.model.request.ValidateCoverageRequest;
import com.mytutorplatform.lessonsservice.model.response.TranscriptResponse;
import com.mytutorplatform.lessonsservice.model.response.ValidateCoverageResponse;
import com.mytutorplatform.lessonsservice.repository.ListeningTranscriptRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.persistence.EntityNotFoundException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ListeningTranscriptService {
    private static final Pattern WORD_PATTERN = Pattern.compile("[\\p{L}']+");

    private final ListeningTranscriptRepository repository;
    private final VocabularyClient vocabularyClient;
    private final OpenAiClient openAiClient;
    private final ObjectMapper objectMapper;

    @Value("${listening.transcript.targetWpm:140}")
    private int targetWpm;

    @Value("${listening.transcript.maxWords:220}")
    private int maxWords;

    public TranscriptResponse generate(TranscriptGenerateRequest req, UUID teacherId, String requestId) {
        if (req == null || req.wordIds() == null || req.wordIds().isEmpty()) {
            throw new IllegalArgumentException("wordIds is required and must be non-empty");
        }
        List<VocabularyClient.VocabWord> words = vocabularyClient.getWordsByIds(req.wordIds());
        if (words.isEmpty()) {
            throw new IllegalArgumentException("No vocabulary words resolved");
        }

        int durationTarget = Optional.ofNullable(req.durationSecTarget()).orElse(60);
        String language = defaultIfBlank(req.language(), "en-US");
        String cefr = defaultIfBlank(req.cefr(), "B1");
        String theme = defaultIfBlank(req.theme(), "general topic");
        String style = req.style();

        String wordList = words.stream().map(VocabularyClient.VocabWord::getText).collect(Collectors.joining(", "));

        String systemMessage = buildSystemMessage(language, cefr, theme, durationTarget, wordList, style);
        String userMessage = buildUserMessage(cefr);

        OpenAiClient.TranscriptPromptPayload payload = OpenAiClient.TranscriptPromptPayload.builder()
                .systemMessage(systemMessage)
                .userMessage(userMessage)
                .seed(req.seed())
                .build();

        String generated = openAiClient.generateTranscript(payload, requestId);
        String text = postProcessText(generated);

        CoverageResult coverage = computeCoverage(text, words);
        int wordCount = countWords(text);
        int estimatedSec = Math.max(1, Math.round((float) wordCount * 60f / (float) targetWpm));

        UUID transcriptId = UUID.randomUUID();

        Map<String, Object> metadata = new HashMap<>();
        metadata.put("language", language);
        metadata.put("theme", theme);
        metadata.put("cefr", cefr);
        metadata.put("durationSecTarget", durationTarget);

        persist(transcriptId, teacherId, text, words, coverage.coverageMap, metadata);

        return new TranscriptResponse(
                transcriptId,
                text,
                coverage.coverageMap,
                estimatedSec,
                metadata
        );
    }

    public TranscriptResponse update(UUID transcriptId, TranscriptUpdateRequest req, UUID teacherId) {
        ListeningTranscript lt = repository.findById(transcriptId)
                .orElseThrow(() -> new EntityNotFoundException("Transcript not found"));
        if (!lt.getTeacherId().equals(teacherId)) {
            throw new IllegalArgumentException("Transcript does not belong to teacher");
        }
        String text = postProcessText(req.transcript());

        List<VocabularyClient.VocabWord> words = readWordsFromJson(lt.getWordsJson());
        CoverageResult coverage = computeCoverage(text, words);
        lt.setText(text);
        lt.setCoverageJson(writeJson(coverage.coverageMap));
        repository.save(lt);

        Map<String, Object> metadata = readMetadataFromJson(lt.getMetadataJson());
        int estimatedSec = Math.max(1, Math.round((float) countWords(text) * 60f / (float) targetWpm));
        return new TranscriptResponse(lt.getTranscriptId(), text, coverage.coverageMap, estimatedSec, metadata);
    }

    public TranscriptResponse get(UUID transcriptId, UUID teacherId) {
        ListeningTranscript lt = repository.findById(transcriptId)
                .orElseThrow(() -> new EntityNotFoundException("Transcript not found"));
        if (!lt.getTeacherId().equals(teacherId)) {
            throw new IllegalArgumentException("Transcript does not belong to teacher");
        }
        List<VocabularyClient.VocabWord> words = readWordsFromJson(lt.getWordsJson());
        CoverageResult coverage = computeCoverage(lt.getText(), words);
        Map<String, Object> metadata = readMetadataFromJson(lt.getMetadataJson());
        int estimatedSec = Math.max(1, Math.round((float) countWords(lt.getText()) * 60f / (float) targetWpm));
        return new TranscriptResponse(lt.getTranscriptId(), lt.getText(), coverage.coverageMap, estimatedSec, metadata);
    }

    public ValidateCoverageResponse validateCoverage(ValidateCoverageRequest req) {
        if (req == null || !StringUtils.hasText(req.transcript())) {
            throw new IllegalArgumentException("transcript is required");
        }
        List<VocabularyClient.VocabWord> words = vocabularyClient.getWordsByIds(req.wordIds());
        if (words.isEmpty()) {
            throw new IllegalArgumentException("No vocabulary words resolved");
        }
        CoverageResult coverage = computeCoverage(postProcessText(req.transcript()), words);
        List<String> missing = coverage.coverageMap.entrySet().stream()
                .filter(e -> !e.getValue())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        return new ValidateCoverageResponse(coverage.coverageMap, missing);
    }

    private void persist(UUID transcriptId, UUID teacherId, String text,
                         List<VocabularyClient.VocabWord> words,
                         Map<String, Boolean> coverage,
                         Map<String, Object> metadata) {
        ListeningTranscript lt = new ListeningTranscript();
        lt.setTranscriptId(transcriptId);
        lt.setTeacherId(teacherId);
        lt.setText(text);
        lt.setWordsJson(writeJson(words));
        lt.setCoverageJson(writeJson(coverage));
        lt.setMetadataJson(writeJson(metadata));
        repository.save(lt);
    }

    private String buildSystemMessage(String language, String cefr, String theme, int durationSecTarget, String wordList, String style) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an English listening content writer. Produce a short monologue in ")
                .append(language)
                .append(", CEFR ").append(cefr)
                .append(", about ").append(theme).append(". ")
                .append("Target ").append(durationSecTarget).append(" seconds at ~").append(targetWpm)
                .append(" wpm (max ").append(maxWords).append(" words). ")
                .append("Must include these words: ").append(wordList).append(". ")
                .append("Do not include translations, lists, or markup. Output plain text only.");
        if (StringUtils.hasText(style)) {
            sb.append(" Style: ").append(style).append(".");
        }
        return sb.toString();
    }

    private String buildUserMessage(String cefr) {
        return "Generate a cohesive, natural-sounding paragraph that stays on topic and uses every target word exactly as normal words in context (not as a list). Prefer present/past simple tenses and everyday vocabulary appropriate to CEFR "
                + cefr + ". Keep sentences 8–18 words.";
    }

    private String postProcessText(String input) {
        if (input == null) return "";
        String text = input.replaceAll("\\s+", " ").replaceAll("\u00A0", " ").trim();
        // Remove potential JSON wrappers if OpenAI response wasn't parsed
        if (text.startsWith("{") && text.contains("content")) {
            // naive extraction of content value
            int idxContent = text.indexOf("\"content\"");
            if (idxContent >= 0) {
                int idxColon = text.indexOf(":", idxContent);
                int idxQuoteStart = text.indexOf('"', idxColon + 1);
                int idxQuoteEnd = text.indexOf('"', idxQuoteStart + 1);
                if (idxQuoteStart > 0 && idxQuoteEnd > idxQuoteStart) {
                    text = text.substring(idxQuoteStart + 1, idxQuoteEnd);
                }
            }
        }
        return text;
    }

    private int countWords(String text) {
        int count = 0;
        Matcher m = WORD_PATTERN.matcher(text);
        while (m.find()) count++;
        return count;
    }

    private CoverageResult computeCoverage(String transcript, List<VocabularyClient.VocabWord> words) {
        Set<String> tokens = tokenizeToSet(transcript);
        Map<String, Boolean> map = new LinkedHashMap<>();
        for (VocabularyClient.VocabWord w : words) {
            String target = normalize(w.getText());
            boolean present = containsWithPluralNormalization(tokens, target);
            map.put(w.getText(), present);
        }
        return new CoverageResult(map);
    }

    private Set<String> tokenizeToSet(String text) {
        Set<String> set = new HashSet<>();
        Matcher m = WORD_PATTERN.matcher(Optional.ofNullable(text).orElse(""));
        while (m.find()) {
            set.add(normalize(m.group()));
        }
        return set;
    }

    private String normalize(String s) {
        return Optional.ofNullable(s).orElse("").toLowerCase(Locale.ROOT);
    }

    private boolean containsWithPluralNormalization(Set<String> tokens, String target) {
        if (tokens.contains(target)) return true;
        // singularize common forms
        String singular = target;
        if (target.endsWith("es")) singular = target.substring(0, target.length() - 2);
        else if (target.endsWith("s")) singular = target.substring(0, target.length() - 1);
        if (tokens.contains(singular)) return true;
        // also check plural form of target
        String plural = target.endsWith("s") ? target : target + "s";
        return tokens.contains(plural) || tokens.contains(plural + "es");
    }

    private List<VocabularyClient.VocabWord> readWordsFromJson(String json) {
        try {
            VocabularyClient.VocabWord[] arr = objectMapper.readValue(json, VocabularyClient.VocabWord[].class);
            return Arrays.asList(arr);
        } catch (Exception e) {
            log.error("Failed to parse words_json: {}", e.getMessage(), e);
            return List.of();
        }
    }

    private Map<String, Object> readMetadataFromJson(String json) {
        try {
            return objectMapper.readValue(json, Map.class);
        } catch (Exception e) {
            log.error("Failed to parse metadata_json: {}", e.getMessage(), e);
            return Map.of();
        }
    }

    private String writeJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize JSON");
        }
    }

    private String defaultIfBlank(String value, String def) {
        return StringUtils.hasText(value) ? value : def;
    }

    private record CoverageResult(Map<String, Boolean> coverageMap) {}
}
