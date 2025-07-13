package com.mytutorplatform.lessonsservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mytutorplatform.lessonsservice.model.request.GenerateExerciseRequest;
import com.mytutorplatform.lessonsservice.model.response.GenerateExerciseResponse;
import com.mytutorplatform.lessonsservice.model.response.Meta;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_GATEWAY;

/**
 * Service for generating AI-powered grammar exercises using OpenAI's GPT-4o model.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ExerciseAiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${openai.api.key}")
    private String apiKey;

    @Value("${openai.api.url:https://api.openai.com/v1/chat/completions}")
    private String apiUrl;

    private static final String MODEL = "gpt-4o";
    private static final double TEMPERATURE = 0.7;
    private static final int MAX_TOKENS = 800;

    /**
     * Generates a grammar exercise based on the provided request parameters.
     *
     * @param request The request containing exercise generation parameters
     * @return The generated exercise response
     * @throws ResponseStatusException if the AI service is unavailable
     */
    public GenerateExerciseResponse generateExercise(GenerateExerciseRequest request) {
        try {
            // Build the prompt from the template
            List<Map<String, String>> messages = buildPrompt(request);
            
            // Call OpenAI API
            String jsonResponse = callOpenAiApi(messages);
            
            // Parse the response
            return parseResponse(jsonResponse);
        } catch (Exception e) {
            log.error("Error generating exercise: {}", e.getMessage(), e);
            throw new ResponseStatusException(BAD_GATEWAY, "AI unavailable");
        }
    }

    /**
     * Builds the prompt for the OpenAI API using the template and request parameters.
     *
     * @param request The request containing exercise generation parameters
     * @return A list of message objects for the OpenAI API
     */
    private List<Map<String, String>> buildPrompt(GenerateExerciseRequest request) {
        List<Map<String, String>> messages = new ArrayList<>();
        
        // System message
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content",
                """
                        You are an expert ESL content creator.\s
                        Generate cloze-style grammar exercises.\s
                        Use exactly {{n[:placeholder]}} tokens, n starting at 1 in reading order.\s
                        Return ONLY valid JSON matching this TypeScript type:
                        {
                          "html": string,
                          "answers": { [n: number]: string[] }
                        }
                        No additional keys, no markdown.""");
        messages.add(systemMessage);
        
        // User message with parameters
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", 
                "Create " + request.getSentences() + " sentences for " + request.getLevel() + " learners.\n" +
                "Grammar focus: " + request.getGrammarFocus() + ".\n" +
                "Topic / lexical field: " + request.getTopic() + ".\n" +
                "Language: " + request.getLanguage() + ".\n" +
                "Placeholders:\n" +
                "  • If a single correct answer – embed it after colon, e.g. {{1:the}}.\n" +
                "  • If multiple correct answers – separate with '|' symbol, e.g. {{1: the|is|are}}.\n" +
                "Make sentences varied and natural.");
        messages.add(userMessage);
        
        return messages;
    }

    /**
     * Calls the OpenAI API with the provided messages.
     *
     * @param messages The messages to send to the API
     * @return The JSON response from the API
     * @throws RestClientException if there's an error calling the API
     */
    private String callOpenAiApi(List<Map<String, String>> messages) throws RestClientException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);
        
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", MODEL);
        requestBody.put("messages", messages);
        requestBody.put("temperature", TEMPERATURE);
        requestBody.put("max_tokens", MAX_TOKENS);
        
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity(apiUrl, entity, String.class);
        return response.getBody();
    }

    /**
     * Parses the JSON response from the OpenAI API.
     *
     * @param jsonResponse The JSON response from the API
     * @return The parsed exercise response
     * @throws JsonProcessingException if there's an error parsing the JSON
     */
    private GenerateExerciseResponse parseResponse(String jsonResponse) throws JsonProcessingException {
        JsonNode rootNode = objectMapper.readTree(jsonResponse);
        String content = rootNode.path("choices").get(0).path("message").path("content").asText();
        int tokensUsed = rootNode.path("usage").path("total_tokens").asInt();
        
        try {
            // Try to parse the content as JSON
            JsonNode contentNode = objectMapper.readTree(content);
            return buildResponse(contentNode, tokensUsed);
        } catch (JsonProcessingException e) {
            // If parsing fails, retry with a system message to return JSON only
            log.warn("Failed to parse response as JSON, retrying with JSON-only instruction");
            return retryWithJsonOnlyInstruction(content, tokensUsed);
        }
    }

    /**
     * Retries the API call with an additional system message to return JSON only.
     *
     * @param content The content from the previous response
     * @param tokensUsed The number of tokens used in the previous response
     * @return The parsed exercise response
     * @throws JsonProcessingException if there's an error parsing the JSON
     * @throws RestClientException if there's an error calling the API
     */
    private GenerateExerciseResponse retryWithJsonOnlyInstruction(String content, int tokensUsed) 
            throws JsonProcessingException, RestClientException {
        List<Map<String, String>> messages = new ArrayList<>();
        
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", "Return JSON ONLY");
        messages.add(systemMessage);
        
        Map<String, String> userMessage = new HashMap<>();
        userMessage.put("role", "user");
        userMessage.put("content", content);
        messages.add(userMessage);
        
        String retryResponse = callOpenAiApi(messages);
        JsonNode rootNode = objectMapper.readTree(retryResponse);
        String retryContent = rootNode.path("choices").get(0).path("message").path("content").asText();
        int retryTokensUsed = rootNode.path("usage").path("total_tokens").asInt();
        
        // Parse the retry content
        JsonNode contentNode = objectMapper.readTree(retryContent);
        return buildResponse(contentNode, tokensUsed + retryTokensUsed);
    }

    /**
     * Builds the exercise response from the parsed JSON content.
     *
     * @param contentNode The parsed JSON content
     * @param tokensUsed The number of tokens used
     * @return The exercise response
     */
    private GenerateExerciseResponse buildResponse(JsonNode contentNode, int tokensUsed) {
        String html = contentNode.path("html").asText();
        
        Map<Integer, List<String>> answers = new HashMap<>();
        JsonNode answersNode = contentNode.path("answers");
        answersNode.fields().forEachRemaining(entry -> {
            int key = Integer.parseInt(entry.getKey());
            List<String> values = new ArrayList<>();
            entry.getValue().forEach(node -> values.add(node.asText()));
            answers.put(key, values);
        });
        
        Meta meta = Meta.builder()
                .model(MODEL)
                .temp(TEMPERATURE)
                .tokensUsed(tokensUsed)
                .build();
        
        return GenerateExerciseResponse.builder()
                .html(html)
                .answers(answers)
                .meta(meta)
                .build();
    }
}