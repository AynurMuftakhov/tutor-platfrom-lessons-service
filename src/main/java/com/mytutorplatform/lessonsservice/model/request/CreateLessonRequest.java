package com.mytutorplatform.lessonsservice.model.request;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@ToString(callSuper = true)
public class CreateLessonRequest extends LessonsRequest {
    private UUID tutorId;
}
