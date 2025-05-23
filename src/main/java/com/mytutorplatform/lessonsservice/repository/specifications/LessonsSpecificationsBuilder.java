package com.mytutorplatform.lessonsservice.repository.specifications;

import com.mytutorplatform.lessonsservice.model.Lesson;
import com.mytutorplatform.lessonsservice.model.LessonStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class LessonsSpecificationsBuilder {
    public static Specification<Lesson> lessonsByParams(UUID tutorId, UUID studentId, List<LessonStatus> status, OffsetDateTime startDateTime, OffsetDateTime endDateTime) {
        return (root, query, cb) -> {
            Predicate predicate = cb.conjunction();

            if (studentId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("studentId"), studentId));
            }

            if (tutorId != null) {
                predicate = cb.and(predicate, cb.equal(root.get("tutorId"), tutorId));
            }

            if (status != null && !status.isEmpty()) {
                predicate = cb.and(predicate, root.get("status").in(status));
            }

            if (startDateTime != null && endDateTime != null) {
                predicate = cb.and(predicate, cb.between(root.get("dateTime"), startDateTime, endDateTime));
            }

            return predicate;
        };
    }

}
