package com.mytutorplatform.lessonsservice.service;

import com.mytutorplatform.lessonsservice.mapper.LessonContentMapper;
import com.mytutorplatform.lessonsservice.model.Lesson;
import com.mytutorplatform.lessonsservice.model.LessonContent;
import com.mytutorplatform.lessonsservice.model.LessonContentLink;
import com.mytutorplatform.lessonsservice.model.response.LessonContentDto;
import com.mytutorplatform.lessonsservice.model.response.LessonContentLinkDto;
import com.mytutorplatform.lessonsservice.repository.LessonContentLinkRepository;
import com.mytutorplatform.lessonsservice.repository.LessonContentRepository;
import com.mytutorplatform.lessonsservice.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LessonContentLinkService {

    private final LessonContentLinkRepository linkRepository;
    private final LessonRepository lessonRepository;
    private final LessonContentRepository lessonContentRepository;
    private final LessonContentMapper lessonContentMapper;

    @Transactional(readOnly = true)
    public List<LessonContentLinkDto> list(UUID lessonId) {
        List<LessonContentLink> links = linkRepository.findByLessonIdOrderBySortOrder(lessonId);
        return links.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void link(UUID lessonId, UUID lessonContentId) {
        if (linkRepository.existsByLessonIdAndLessonContentId(lessonId, lessonContentId)) {
            return;
        }

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found with id: " + lessonId));
        LessonContent lessonContent = lessonContentRepository.findById(lessonContentId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson content not found with id: " + lessonContentId));

        Integer maxSortOrder = linkRepository.findMaxSortOrderByLessonId(lessonId);
        int newSortOrder = (maxSortOrder != null) ? maxSortOrder + 1 : 0;

        LessonContentLink link = LessonContentLink.builder()
                .lesson(lesson)
                .lessonContent(lessonContent)
                .sortOrder(newSortOrder)
                .build();
        linkRepository.save(link);
    }

    @Transactional
    public void unlink(UUID linkId) {
        linkRepository.deleteById(linkId);
    }

    private LessonContentLinkDto toDto(LessonContentLink link) {
        LessonContentDto lcDto = lessonContentMapper.toDto(link.getLessonContent());
        return LessonContentLinkDto.builder()
                .id(link.getId())
                .lessonContent(lcDto)
                .sortOrder(link.getSortOrder())
                .build();
    }
}
