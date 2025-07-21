package com.mytutorplatform.lessonsservice.service;

import com.mytutorplatform.lessonsservice.model.GrammarItem;
import com.mytutorplatform.lessonsservice.model.Material;
import com.mytutorplatform.lessonsservice.model.request.AttemptDto;
import com.mytutorplatform.lessonsservice.model.request.GrammarScoreRequest;
import com.mytutorplatform.lessonsservice.model.response.GapResultDto;
import com.mytutorplatform.lessonsservice.model.response.GrammarScoreResponse;
import com.mytutorplatform.lessonsservice.model.response.ItemScoreDto;
import com.mytutorplatform.lessonsservice.repository.GrammarItemRepository;
import com.mytutorplatform.lessonsservice.repository.MaterialRepository;
import com.mytutorplatform.lessonsservice.util.AnswerComparator;
import com.mytutorplatform.lessonsservice.util.AnswerParser;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GrammarScoringService {

    private final GrammarItemRepository grammarItemRepository;
    private final MaterialRepository materialRepository;

    /**
     * Scores a student's attempts at grammar items in a material.
     *
     * @param materialId The ID of the material containing the grammar items
     * @param attempts The student's attempts at each grammar item
     * @return A detailed score report
     */
    public GrammarScoreResponse score(UUID materialId, List<AttemptDto> attempts) {
        // Validate material exists
        materialRepository.findById(materialId)
                .orElseThrow(() -> new EntityNotFoundException("Material not found with id: " + materialId));

        // Load all grammar items for the material
        List<GrammarItem> grammarItems = grammarItemRepository.findByMaterialId(materialId);
        
        if (grammarItems.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "No grammar items found for material with id: " + materialId);
        }

        // Create a map of grammar item IDs to grammar items for easy lookup
        Map<UUID, GrammarItem> grammarItemMap = grammarItems.stream()
                .collect(Collectors.toMap(GrammarItem::getId, item -> item));

        // Validate that all grammar items are included in the attempts
        validateAttempts(grammarItemMap.keySet(), attempts);

        // Score each attempt and build the response
        List<ItemScoreDto> itemScores = new ArrayList<>();
        int totalGaps = 0;
        int correctGaps = 0;
        int correctItems = 0;

        for (AttemptDto attempt : attempts) {
            GrammarItem item = grammarItemMap.get(attempt.getGrammarItemId());
            
            // Parse the answer string into a 2D list of acceptable answers
            List<List<String>> acceptableAnswers = AnswerParser.parseAnswers(item.getAnswer());
            
            // Validate that the number of gap answers matches the number of gaps
            if (attempt.getGapAnswers().size() != acceptableAnswers.size()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                        "Number of gap answers (" + attempt.getGapAnswers().size() + 
                        ") does not match number of gaps (" + acceptableAnswers.size() + 
                        ") for grammar item with id: " + item.getId());
            }

            // Score each gap
            List<GapResultDto> gapResults = new ArrayList<>();
            boolean itemCorrect = true;
            
            for (int i = 0; i < attempt.getGapAnswers().size(); i++) {
                String studentAnswer = attempt.getGapAnswers().get(i);
                List<String> correctAnswers = acceptableAnswers.get(i);
                boolean isCorrect = AnswerComparator.isCorrect(studentAnswer, correctAnswers);

                // For the response, we'll show the first correct answer as "the correct answer"
                String correctAnswer = isCorrect? studentAnswer : correctAnswers.isEmpty() ? "" : correctAnswers.get(0);
                
                gapResults.add(GapResultDto.builder()
                        .index(i)
                        .student(studentAnswer)
                        .correct(correctAnswer)
                        .isCorrect(isCorrect)
                        .build());
                
                totalGaps++;
                if (isCorrect) {
                    correctGaps++;
                } else {
                    itemCorrect = false;
                }
            }
            
            itemScores.add(ItemScoreDto.builder()
                    .grammarItemId(item.getId())
                    .gapResults(gapResults)
                    .itemCorrect(itemCorrect)
                    .build());
            
            if (itemCorrect) {
                correctItems++;
            }
        }

        // Build and return the response
        return GrammarScoreResponse.builder()
                .materialId(materialId)
                .totalItems(grammarItems.size())
                .correctItems(correctItems)
                .totalGaps(totalGaps)
                .correctGaps(correctGaps)
                .details(itemScores)
                .build();
    }

    /**
     * Validates that all grammar items are included in the attempts and that there are no duplicates.
     *
     * @param grammarItemIds The IDs of all grammar items in the material
     * @param attempts The student's attempts
     * @throws ResponseStatusException if validation fails
     */
    private void validateAttempts(Set<UUID> grammarItemIds, List<AttemptDto> attempts) {
        // Check for missing grammar items
        Set<UUID> attemptedItemIds = attempts.stream()
                .map(AttemptDto::getGrammarItemId)
                .collect(Collectors.toSet());
        
        Set<UUID> missingItemIds = new HashSet<>(grammarItemIds);
        missingItemIds.removeAll(attemptedItemIds);
        
        if (!missingItemIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Missing attempts for grammar items with ids: " + missingItemIds);
        }
        
        // Check for extra grammar items
        Set<UUID> extraItemIds = new HashSet<>(attemptedItemIds);
        extraItemIds.removeAll(grammarItemIds);
        
        if (!extraItemIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Attempts for non-existent grammar items with ids: " + extraItemIds);
        }
        
        // Check for duplicate grammar items
        Set<UUID> uniqueAttemptedItemIds = new HashSet<>();
        Set<UUID> duplicateItemIds = attempts.stream()
                .map(AttemptDto::getGrammarItemId)
                .filter(id -> !uniqueAttemptedItemIds.add(id))
                .collect(Collectors.toSet());
        
        if (!duplicateItemIds.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, 
                    "Duplicate attempts for grammar items with ids: " + duplicateItemIds);
        }
    }
}