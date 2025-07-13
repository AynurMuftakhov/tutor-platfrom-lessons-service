package com.mytutorplatform.lessonsservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mytutorplatform.lessonsservice.model.request.GenerateExerciseRequest;
import com.mytutorplatform.lessonsservice.model.response.GenerateExerciseResponse;
import com.mytutorplatform.lessonsservice.model.response.Meta;
import com.mytutorplatform.lessonsservice.service.ExerciseAiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class ExerciseAiControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private ExerciseAiService exerciseAiService;

    @InjectMocks
    private ExerciseAiController exerciseAiController;

    @BeforeEach
    public void setup() {
        objectMapper = new ObjectMapper();
        mockMvc = MockMvcBuilders.standaloneSetup(exerciseAiController).build();
    }

    @Test
    public void testGenerateExercise_Success() throws Exception {
        // Prepare test data
        GenerateExerciseRequest request = new GenerateExerciseRequest(
                "articles",
                "geographic entities",
                "intermediate",
                5,
                "en",
                "doubleBraces"
        );

        Map<Integer, List<String>> answers = new HashMap<>();
        answers.put(1, List.of("the"));
        answers.put(2, List.of("a", "an"));

        Meta meta = Meta.builder()
                .model("gpt-4o")
                .temp(0.7)
                .tokensUsed(150)
                .build();

        GenerateExerciseResponse response = GenerateExerciseResponse.builder()
                .html("Mount {{1:the}} Everest is {{2:a}} tall mountain.")
                .answers(answers)
                .meta(meta)
                .build();

        // Mock service behavior
        when(exerciseAiService.generateExercise(any(GenerateExerciseRequest.class)))
                .thenReturn(response);

        // Perform request and verify response
        mockMvc.perform(post("/api/ai/exercises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.html").value("Mount {{1:the}} Everest is {{2:a}} tall mountain."))
                .andExpect(jsonPath("$.answers.1[0]").value("the"))
                .andExpect(jsonPath("$.answers.2[0]").value("a"))
                .andExpect(jsonPath("$.answers.2[1]").value("an"))
                .andExpect(jsonPath("$.meta.model").value("gpt-4o"))
                .andExpect(jsonPath("$.meta.temp").value(0.7))
                .andExpect(jsonPath("$.meta.tokensUsed").value(150));
    }

    @Test
    public void testGenerateExercise_ValidationFailure() throws Exception {
        // Prepare invalid request (missing required fields)
        GenerateExerciseRequest request = new GenerateExerciseRequest(
                "",  // Empty grammar focus (should fail validation)
                "geographic entities",
                "intermediate",
                5,
                "en",
                "doubleBraces"
        );

        // Perform request and verify validation error
        mockMvc.perform(post("/api/ai/exercises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGenerateExercise_ServiceFailure() throws Exception {
        // Prepare test data
        GenerateExerciseRequest request = new GenerateExerciseRequest(
                "articles",
                "geographic entities",
                "intermediate",
                5,
                "en",
                "doubleBraces"
        );

        // Mock service failure
        when(exerciseAiService.generateExercise(any(GenerateExerciseRequest.class)))
                .thenThrow(new ResponseStatusException(BAD_GATEWAY, "AI unavailable"));

        // Perform request and verify error response
        mockMvc.perform(post("/api/ai/exercises")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadGateway());
    }
}
