package com.mytutorplatform.lessonsservice.service;

import com.mytutorplatform.lessonsservice.mapper.GrammarTypeMapper;
import com.mytutorplatform.lessonsservice.model.GrammarItem;
import com.mytutorplatform.lessonsservice.model.request.CreateGrammarItemRequest;
import com.mytutorplatform.lessonsservice.model.request.CreateMultipleChoiceItemRequest;
import com.mytutorplatform.lessonsservice.model.response.GrammarItemDto;
import com.mytutorplatform.lessonsservice.model.response.MultipleChoiceItemDto;
import com.mytutorplatform.lessonsservice.repository.GrammarItemRepository;
import com.mytutorplatform.lessonsservice.repository.MaterialRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GrammarItemService {

    private final GrammarItemRepository repository;
    private final GrammarTypeMapper grammarTypeMapper;
    private final MaterialRepository materialRepository;

    /**
     * Creates a new grammar item of type GAP_FILL
     */
    public GrammarItemDto createItem(UUID materialId, CreateGrammarItemRequest item) {
        if (materialId == null) {
            throw new IllegalArgumentException("Material id cannot be null");
        }

        // Verify that the material exists
        materialRepository.findById(materialId)
                .orElseThrow(() -> new EntityNotFoundException("Material not found with id: " + materialId));

        GrammarItem grammarItem = grammarTypeMapper.map(materialId, item);

        return grammarTypeMapper.map(repository.save(grammarItem));
    }

    /**
     * Creates a new grammar item of type MULTIPLE_CHOICE
     */
    public MultipleChoiceItemDto createMultipleChoiceItem(UUID materialId, CreateMultipleChoiceItemRequest item) {
        if (materialId == null) {
            throw new IllegalArgumentException("Material id cannot be null");
        }

        // Verify that the material exists
        materialRepository.findById(materialId)
                .orElseThrow(() -> new EntityNotFoundException("Material not found with id: " + materialId));

        GrammarItem grammarItem = grammarTypeMapper.mapMultipleChoice(materialId, item);

        return grammarTypeMapper.mapToMultipleChoice(repository.save(grammarItem));
    }

    public void deleteItem(UUID itemId) {
        repository.deleteById(itemId);
    }

    /**
     * Gets all grammar items for a material, returning the appropriate DTO type based on the item type
     */
    public List<Object> getItemsByMaterialId(UUID materialId) {
        if (materialId == null) {
            throw new IllegalArgumentException("Material id cannot be null");
        }

        List<GrammarItem> items = repository.findByMaterialId(materialId);
        return grammarTypeMapper.mapToAppropriateDtos(items);
    }

    /**
     * Gets all grammar items for a material as GrammarItemDto (for backward compatibility)
     */
    public List<GrammarItemDto> getGrammarItemDtosByMaterialId(UUID materialId) {
        if (materialId == null) {
            throw new IllegalArgumentException("Material id cannot be null");
        }

        return grammarTypeMapper.mapList(repository.findByMaterialId(materialId));
    }
}
