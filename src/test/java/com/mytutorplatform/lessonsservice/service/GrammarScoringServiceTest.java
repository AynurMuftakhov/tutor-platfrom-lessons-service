package com.mytutorplatform.lessonsservice.service;

import com.mytutorplatform.lessonsservice.model.GrammarItem;
import com.mytutorplatform.lessonsservice.model.request.AttemptDto;
import com.mytutorplatform.lessonsservice.model.response.GrammarScoreResponse;
import com.mytutorplatform.lessonsservice.model.response.ItemScoreDto;
import com.mytutorplatform.lessonsservice.repository.GrammarItemRepository;
import com.mytutorplatform.lessonsservice.repository.MaterialRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GrammarScoringServiceTest {

    @Mock
    private GrammarItemRepository grammarItemRepository;

    @Mock
    private MaterialRepository materialRepository;

    @InjectMocks
    private GrammarScoringService scoringService;

    private UUID materialId;
    private UUID gapFillItemId;
    private UUID mcqItemId;
    private GrammarItem gapFillItem;
    private GrammarItem mcqItem;

    @BeforeEach
    void setUp() {
        materialId = UUID.randomUUID();
        gapFillItemId = UUID.randomUUID();
        mcqItemId = UUID.randomUUID();

        // Set up a gap fill item
        gapFillItem = GrammarItem.builder()
                .id(gapFillItemId)
                .materialId(materialId)
                .type(GrammarItem.Type.GAP_FILL)
                .text("I [live] in London.")
                .answer("live|am living")
                .build();

        // Set up a multiple choice item
        String[] options = {"Option A", "Option B", "Option C", "Option D"};
        mcqItem = GrammarItem.builder()
                .id(mcqItemId)
                .materialId(materialId)
                .type(GrammarItem.Type.MULTIPLE_CHOICE)
                .question("What is the correct option?")
                .options(options)
                .correctIndex((short) 2)
                .build();

        // Mock repository responses
        when(materialRepository.findById(materialId)).thenReturn(Optional.of(mock(com.mytutorplatform.lessonsservice.model.Material.class)));
        when(grammarItemRepository.findByMaterialId(materialId)).thenReturn(Arrays.asList(gapFillItem, mcqItem));
    }

    @Test
    void shouldScoreGapFillItemCorrectly() {
        // Given
        AttemptDto gapFillAttempt = new AttemptDto();
        gapFillAttempt.setGrammarItemId(gapFillItemId);
        gapFillAttempt.setGapAnswers(List.of("live"));

        AttemptDto mcqAttempt = new AttemptDto();
        mcqAttempt.setGrammarItemId(mcqItemId);
        mcqAttempt.setChosenIndex((short) 2); // Correct answer

        List<AttemptDto> attempts = Arrays.asList(gapFillAttempt, mcqAttempt);

        // When
        GrammarScoreResponse response = scoringService.score(materialId, attempts);

        // Then
        assertNotNull(response);
        assertEquals(materialId, response.getMaterialId());
        assertEquals(2, response.getTotalItems());
        assertEquals(2, response.getCorrectItems()); // Both items are correct
        assertEquals(2, response.getTotalGaps()); // 1 gap in gap fill + 1 "gap" for MCQ
        assertEquals(2, response.getCorrectGaps()); // Both gaps are correct

        // Verify item scores
        List<ItemScoreDto> details = response.getDetails();
        assertEquals(2, details.size());

        // Find the gap fill item score
        ItemScoreDto gapFillScore = details.stream()
                .filter(item -> item.getGrammarItemId().equals(gapFillItemId))
                .findFirst()
                .orElseThrow();

        assertTrue(gapFillScore.isItemCorrect());
        assertEquals(1, gapFillScore.getGapResults().size());
        assertTrue(gapFillScore.getGapResults().get(0).isCorrect());
        assertEquals("live", gapFillScore.getGapResults().get(0).getStudent());

        // Find the MCQ item score
        ItemScoreDto mcqScore = details.stream()
                .filter(item -> item.getGrammarItemId().equals(mcqItemId))
                .findFirst()
                .orElseThrow();

        assertTrue(mcqScore.isItemCorrect());
        assertEquals(1, mcqScore.getGapResults().size());
        assertTrue(mcqScore.getGapResults().get(0).isCorrect());
        assertEquals("2", mcqScore.getGapResults().get(0).getStudent());
        assertEquals("2", mcqScore.getGapResults().get(0).getCorrectAnswer());
    }

    @Test
    void shouldScoreGapFillItemIncorrectly() {
        // Given
        AttemptDto gapFillAttempt = new AttemptDto();
        gapFillAttempt.setGrammarItemId(gapFillItemId);
        gapFillAttempt.setGapAnswers(List.of("lived")); // Incorrect answer

        AttemptDto mcqAttempt = new AttemptDto();
        mcqAttempt.setGrammarItemId(mcqItemId);
        mcqAttempt.setChosenIndex((short) 1); // Incorrect answer

        List<AttemptDto> attempts = Arrays.asList(gapFillAttempt, mcqAttempt);

        // When
        GrammarScoreResponse response = scoringService.score(materialId, attempts);

        // Then
        assertNotNull(response);
        assertEquals(materialId, response.getMaterialId());
        assertEquals(2, response.getTotalItems());
        assertEquals(0, response.getCorrectItems()); // Both items are incorrect
        assertEquals(2, response.getTotalGaps()); // 1 gap in gap fill + 1 "gap" for MCQ
        assertEquals(0, response.getCorrectGaps()); // Both gaps are incorrect

        // Verify item scores
        List<ItemScoreDto> details = response.getDetails();
        assertEquals(2, details.size());

        // Find the gap fill item score
        ItemScoreDto gapFillScore = details.stream()
                .filter(item -> item.getGrammarItemId().equals(gapFillItemId))
                .findFirst()
                .orElseThrow();

        assertFalse(gapFillScore.isItemCorrect());
        assertEquals(1, gapFillScore.getGapResults().size());
        assertFalse(gapFillScore.getGapResults().get(0).isCorrect());
        assertEquals("lived", gapFillScore.getGapResults().get(0).getStudent());
        assertEquals("live", gapFillScore.getGapResults().get(0).getCorrectAnswer());

        // Find the MCQ item score
        ItemScoreDto mcqScore = details.stream()
                .filter(item -> item.getGrammarItemId().equals(mcqItemId))
                .findFirst()
                .orElseThrow();

        assertFalse(mcqScore.isItemCorrect());
        assertEquals(1, mcqScore.getGapResults().size());
        assertFalse(mcqScore.getGapResults().get(0).isCorrect());
        assertEquals("1", mcqScore.getGapResults().get(0).getStudent());
        assertEquals("2", mcqScore.getGapResults().get(0).getCorrectAnswer());
    }

    @Test
    void shouldThrowExceptionWhenMissingChosenIndex() {
        // Given
        AttemptDto gapFillAttempt = new AttemptDto();
        gapFillAttempt.setGrammarItemId(gapFillItemId);
        gapFillAttempt.setGapAnswers(List.of("live"));

        AttemptDto mcqAttempt = new AttemptDto();
        mcqAttempt.setGrammarItemId(mcqItemId);
        // Missing chosenIndex

        List<AttemptDto> attempts = Arrays.asList(gapFillAttempt, mcqAttempt);

        // When/Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            scoringService.score(materialId, attempts);
        });

        assertTrue(exception.getMessage().contains("Missing chosenIndex for MULTIPLE_CHOICE item"));
    }

    @Test
    void shouldThrowExceptionWhenMissingGapAnswers() {
        // Given
        AttemptDto gapFillAttempt = new AttemptDto();
        gapFillAttempt.setGrammarItemId(gapFillItemId);
        // Missing gapAnswers

        AttemptDto mcqAttempt = new AttemptDto();
        mcqAttempt.setGrammarItemId(mcqItemId);
        mcqAttempt.setChosenIndex((short) 2);

        List<AttemptDto> attempts = Arrays.asList(gapFillAttempt, mcqAttempt);

        // When/Then
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> {
            scoringService.score(materialId, attempts);
        });

        assertTrue(exception.getMessage().contains("Missing gapAnswers for GAP_FILL item"));
    }
}