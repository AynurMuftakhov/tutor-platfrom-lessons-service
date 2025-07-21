package com.mytutorplatform.lessonsservice.service;

import com.mytutorplatform.lessonsservice.mapper.GrammarTypeMapper;
import com.mytutorplatform.lessonsservice.model.GrammarItem;
import com.mytutorplatform.lessonsservice.model.request.CreateGrammarItemRequest;
import com.mytutorplatform.lessonsservice.model.response.GrammarItemDto;
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

    public GrammarItemDto createItem(UUID materialId, CreateGrammarItemRequest item){
        if (materialId == null) {
            throw new IllegalArgumentException("Material id cannot be null");
        }

        // Verify that the material exists
        materialRepository.findById(materialId)
                    .orElseThrow(() -> new EntityNotFoundException("Material not found with id: " + materialId));

        GrammarItem grammarItem = grammarTypeMapper.map(materialId, item);

        return grammarTypeMapper.map(repository.save(grammarItem));

    }

    public void deleteItem(UUID itemId){
        repository.deleteById(itemId);
    }

    public List<GrammarItemDto> getItemsByMaterialId(UUID materialId){
        if (materialId == null) {
            throw new IllegalArgumentException("Material id cannot be null");
        }

        return grammarTypeMapper.mapList(repository.findByMaterialId(materialId));
    }


}
