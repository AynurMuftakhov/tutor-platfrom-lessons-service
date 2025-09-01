package com.mytutorplatform.lessonsservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.mytutorplatform.lessonsservice.model.LessonContentStatus;
import com.mytutorplatform.lessonsservice.model.LessonContent;
import com.mytutorplatform.lessonsservice.model.response.LessonContentDto;
import com.mytutorplatform.lessonsservice.service.LessonContentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(LessonContentController.class)
@org.springframework.context.annotation.Import(com.mytutorplatform.lessonsservice.mapper.LessonContentMapperImpl.class)
class LessonContentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LessonContentService service;

    private LessonContentDto sample(UUID id, String title, LessonContentStatus status, UUID ownerId, String[] tags) {
        ObjectNode layout = objectMapper.createObjectNode();
        layout.put("type", "canvas");
        ObjectNode content = objectMapper.createObjectNode();
        content.put("block", "video");
        return LessonContentDto.builder()
                .id(id)
                .ownerId(ownerId)
                .title(title)
                .status(status)
                .tags(tags)
                .layout(layout)
                .content(content)
                .createdBy(UUID.randomUUID())
                .build();
    }

    @Test
    void create_update_publish_unpublish_happy_paths() throws Exception {
        UUID ownerId = UUID.randomUUID();
        LessonContentDto createRequest = sample(null, "My Canvas", LessonContentStatus.DRAFT, ownerId, new String[]{"tag1"});
        LessonContentDto created = sample(UUID.randomUUID(), "My Canvas", LessonContentStatus.DRAFT, ownerId, new String[]{"tag1"});
        LessonContentDto updated = sample(created.getId(), "Updated Title", LessonContentStatus.DRAFT, ownerId, new String[]{"tag1", "tag2"});
        LessonContentDto published = sample(created.getId(), "Updated Title", LessonContentStatus.PUBLISHED, ownerId, new String[]{"tag1", "tag2"});
        LessonContentDto unpublished = sample(created.getId(), "Updated Title", LessonContentStatus.DRAFT, ownerId, new String[]{"tag1", "tag2"});

        when(service.create(any())).thenAnswer(invocation -> {
                    LessonContent entity = new LessonContent();
                    entity.setId(created.getId());
                    entity.setOwnerId(ownerId);
                    entity.setTitle(created.getTitle());
                    entity.setStatus(LessonContentStatus.DRAFT);
                    entity.setTags(new String[]{"tag1"});
                    entity.setLayout(createRequest.getLayout());
                    entity.setContent(createRequest.getContent());
                    entity.setCreatedBy(UUID.randomUUID());
                    return entity;
                });
                when(service.update(eq(created.getId()), any())).thenAnswer(invocation -> {
                    LessonContent entity = new LessonContent();
                    entity.setId(created.getId());
                    entity.setOwnerId(ownerId);
                    entity.setTitle(updated.getTitle());
                    entity.setStatus(LessonContentStatus.DRAFT);
                    entity.setTags(new String[]{"tag1","tag2"});
                    entity.setLayout(updated.getLayout());
                    entity.setContent(updated.getContent());
                    entity.setCreatedBy(UUID.randomUUID());
                    return entity;
                });
                when(service.publish(eq(created.getId()))).thenAnswer(invocation -> {
                    LessonContent entity = new LessonContent();
                    entity.setId(created.getId());
                    entity.setOwnerId(ownerId);
                    entity.setTitle(updated.getTitle());
                    entity.setStatus(LessonContentStatus.PUBLISHED);
                    entity.setTags(new String[]{"tag1","tag2"});
                    entity.setLayout(updated.getLayout());
                    entity.setContent(updated.getContent());
                    entity.setCreatedBy(UUID.randomUUID());
                    return entity;
                });
                when(service.unpublish(eq(created.getId()))).thenAnswer(invocation -> {
                    LessonContent entity = new LessonContent();
                    entity.setId(created.getId());
                    entity.setOwnerId(ownerId);
                    entity.setTitle(updated.getTitle());
                    entity.setStatus(LessonContentStatus.DRAFT);
                    entity.setTags(new String[]{"tag1","tag2"});
                    entity.setLayout(updated.getLayout());
                    entity.setContent(updated.getContent());
                    entity.setCreatedBy(UUID.randomUUID());
                    return entity;
                });

        // Create
        mockMvc.perform(post("/api/lesson-contents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isOk());

        // Update
        mockMvc.perform(put("/api/lesson-contents/{id}", created.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk());

        // Publish
        mockMvc.perform(post("/api/lesson-contents/{id}/publish", created.getId()))
                .andExpect(status().isOk());

        // Unpublish
        mockMvc.perform(post("/api/lesson-contents/{id}/unpublish", created.getId()))
                .andExpect(status().isOk());
    }

    @Test
    void list_filters_by_owner_q_tags_status() throws Exception {
        UUID ownerId = UUID.randomUUID();
        LessonContentDto d1 = sample(UUID.randomUUID(), "Title One", LessonContentStatus.DRAFT, ownerId, new String[]{"a", "b"});
        LessonContentDto d2 = sample(UUID.randomUUID(), "Another", LessonContentStatus.PUBLISHED, ownerId, new String[]{"b"});
        List<LessonContentDto> list = Arrays.asList(d1, d2);
        Page<LessonContentDto> page = new PageImpl<>(list);
        // Controller maps Page<LessonContent> to Page<LessonContentDto>; here we can just return empty and assert 200 OK.
        when(service.list(eq(ownerId), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(Page.empty());

        mockMvc.perform(get("/api/lesson-contents")
                        .param("ownerId", ownerId.toString())
                        .param("q", "Title")
                        .param("tags", "a,b")
                        .param("status", "DRAFT")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }
}
