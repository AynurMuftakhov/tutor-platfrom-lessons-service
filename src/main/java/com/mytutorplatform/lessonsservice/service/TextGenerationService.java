package com.mytutorplatform.lessonsservice.service;

import com.mytutorplatform.lessonsservice.model.request.TextGenerationRequest;
import com.mytutorplatform.lessonsservice.model.response.TextGenerationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TextGenerationService {

    private final OpenAiClient openAiClient;

    @Value("${lesson.text.maxWords:500}")
    private int defaultMaxWords;

    public TextGenerationResponse generate(TextGenerationRequest request, UUID teacherId, String requestId) {
        if (request == null || !StringUtils.hasText(request.prompt())) {
            throw new IllegalArgumentException("prompt is required");
        }

        String prompt = request.prompt().trim();
        String existing = normalizeExisting(request.existingText());

        List<Map<String, Object>> messages = buildMessages(prompt, existing, request.lessonTitle(), request.level());
        String aiHtml = openAiClient.generateText(messages, 0.62, null, null, requestId);
        String sanitized = postProcessHtml(aiHtml);

        Map<String, Object> meta = new HashMap<>();
        meta.put("requestId", requestId);
        meta.put("source", "openai");

        return TextGenerationResponse.builder()
                .html(sanitized)
                .meta(meta)
                .build();
    }

    private List<Map<String, Object>> buildMessages(String prompt, String existingHtml, String lessonTitle, String level) {
        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", buildSystemMessage(level)));
        if (StringUtils.hasText(existingHtml)) {
            messages.add(Map.of("role", "user", "content", "Existing lesson text (HTML) to refine:\n" + existingHtml));
            messages.add(Map.of("role", "assistant", "content", "I will refine this text according to the next teacher request while keeping it concise and student-friendly."));
        }
        messages.add(Map.of("role", "user", "content", buildUserMessage(prompt, existingHtml, lessonTitle)));
        return messages;
    }

    private String buildSystemMessage(String level) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an experienced English language methodologist creating lesson text for students. ");
        sb.append("Write clear, encouraging explanations or short reading passages that a teacher can drop directly into a lesson. ");
        sb.append("Use simple HTML only (<p>, <ul>, <ol>, <li>, <strong>, <em>), no markdown, tables, or code fences. ");
        sb.append("Avoid scripts, links, or images. ");
        sb.append("Unless the teacher asks otherwise, aim for roughly ").append(defaultMaxWords).append(" words. ");
        if (StringUtils.hasText(level)) {
            sb.append("Keep the language appropriate for ").append(level).append(" learners. ");
        }
        sb.append("If a draft is provided, improve it rather than discarding it; keep useful structure.");
        return sb.toString();
    }

    private String buildUserMessage(String prompt, String existingHtml, String lessonTitle) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.hasText(lessonTitle)) {
            sb.append("Lesson topic/title: ").append(lessonTitle.trim()).append(". ");
        }
        if (StringUtils.hasText(existingHtml)) {
            sb.append("Revise the existing text per the teacher request. Preserve good explanations and structure where it helps the learner. ");
        } else {
            sb.append("Write a fresh piece of text for the request. ");
        }
        sb.append("Teacher request: ").append(prompt).append("\n");
        sb.append("Respond with HTML only, no JSON or markdown.");
        return sb.toString();
    }

    private String normalizeExisting(String html) {
        if (!StringUtils.hasText(html)) return null;
        String cleaned = html
                .replaceAll("(?is)<script.*?>.*?</script>", " ")
                .replaceAll("(?is)<style.*?>.*?</style>", " ");
        cleaned = cleaned.replaceAll("\\s+", " ").trim();
        return cleaned.isBlank() ? null : cleaned;
    }

    private String postProcessHtml(String raw) {
        if (raw == null) return "";
        String trimmed = stripCodeFences(raw).trim();
        String withoutScripts = trimmed
                .replaceAll("(?is)<script.*?>.*?</script>", "")
                .replaceAll("(?is)<style.*?>.*?</style>", "");
        boolean containsHtmlTags = withoutScripts.matches("(?is).*<\\w+[^>]*>.*");
        if (containsHtmlTags) {
            return withoutScripts;
        }
        // Convert plain text into paragraphs
        List<String> paragraphs = Arrays.stream(withoutScripts.split("\\n{2,}"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
        if (paragraphs.isEmpty() && StringUtils.hasText(withoutScripts)) {
            paragraphs = List.of(withoutScripts.trim());
        }
        return paragraphs.stream()
                .map(this::escapeHtml)
                .map(p -> "<p>" + p + "</p>")
                .collect(Collectors.joining("\n"));
    }

    private String stripCodeFences(String text) {
        if (text == null) return "";
        return text.replaceAll("(?is)^```(?:html)?\\s*", "")
                .replaceAll("(?is)```\\s*$", "");
    }

    private String escapeHtml(String text) {
        return text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
