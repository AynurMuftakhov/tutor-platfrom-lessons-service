package com.mytutorplatform.lessonsservice.service;

import com.mytutorplatform.lessonsservice.mapper.ListeningTaskMapper;
import com.mytutorplatform.lessonsservice.mapper.MaterialMapper;
import com.mytutorplatform.lessonsservice.model.Lesson;
import com.mytutorplatform.lessonsservice.model.LessonMaterial;
import com.mytutorplatform.lessonsservice.model.ListeningTask;
import com.mytutorplatform.lessonsservice.model.Material;
import com.mytutorplatform.lessonsservice.model.response.LessonMaterialDto;
import com.mytutorplatform.lessonsservice.model.response.ListeningTaskDTO;
import com.mytutorplatform.lessonsservice.model.response.MaterialDTO;
import com.mytutorplatform.lessonsservice.repository.LessonMaterialRepository;
import com.mytutorplatform.lessonsservice.repository.LessonRepository;
import com.mytutorplatform.lessonsservice.repository.ListeningTaskRepository;
import com.mytutorplatform.lessonsservice.repository.MaterialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonMaterialService {

    private final LessonMaterialRepository lessonMaterialRepository;
    private final LessonRepository lessonRepository;
    private final MaterialRepository materialRepository;
    private final ListeningTaskRepository listeningTaskRepository;
    private final MaterialMapper materialMapper;
    private final ListeningTaskMapper listeningTaskMapper;

    public List<LessonMaterialDto> list(UUID lessonId) {
        List<LessonMaterial> lessonMaterials = lessonMaterialRepository.findByLessonIdOrderBySortOrder(lessonId);
        
        return lessonMaterials.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void link(UUID lessonId, UUID materialId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found with id: " + lessonId));
        
        Material material = materialRepository.findById(materialId)
                .orElseThrow(() -> new IllegalArgumentException("Material not found with id: " + materialId));
        
        Integer maxSortOrder = lessonMaterialRepository.findMaxSortOrderByLessonId(lessonId);
        int newSortOrder = (maxSortOrder != null) ? maxSortOrder + 1 : 0;
        
        LessonMaterial lessonMaterial = LessonMaterial.builder()
                .lesson(lesson)
                .material(material)
                .sortOrder(newSortOrder)
                .build();
                
        lessonMaterialRepository.save(lessonMaterial);
    }

    @Transactional
    public void unlink(UUID linkId) {
        lessonMaterialRepository.deleteById(linkId);
    }

    @Transactional
    public void reorder(UUID linkId, int newOrder) {
        LessonMaterial lessonMaterial = lessonMaterialRepository.findById(linkId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson material link not found with id: " + linkId));
        
        lessonMaterial.setSortOrder(newOrder);
        lessonMaterialRepository.save(lessonMaterial);
    }
    
    private LessonMaterialDto convertToDto(LessonMaterial lessonMaterial) {
        List<ListeningTask> tasks = listeningTaskRepository.findByMaterialId(lessonMaterial.getMaterial().getId());
        MaterialDTO materialDTO = materialMapper.map(lessonMaterial.getMaterial());
        List<ListeningTaskDTO> listeningTaskDTOS = listeningTaskMapper.mapList(tasks);
        return new LessonMaterialDto(
                lessonMaterial.getId(),
                materialDTO,
                listeningTaskDTOS
        );
    }
}