package com.mytutorplatform.lessonsservice.model.response;

import java.util.List;
import java.util.UUID;

public record LessonMaterialDto(UUID id,
                                MaterialDTO material,
                                List<ListeningTaskDTO> tasks) {}