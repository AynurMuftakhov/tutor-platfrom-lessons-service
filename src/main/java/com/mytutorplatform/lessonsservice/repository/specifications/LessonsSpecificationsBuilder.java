package com.mytutorplatform.lessonsservice.repository.specifications;

import com.mytutorplatform.lessonsservice.model.Lesson;
import com.mytutorplatform.lessonsservice.model.LessonStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public class LessonsSpecificationsBuilder {
    public static Specification<Lesson> lessonsByParams(UUID tutorId, UUID studentId, List<LessonStatus> status, OffsetDateTime startDateTime, OffsetDateTime endDateTime, OffsetDateTime currentDate) {
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

            if (startDateTime != null && endDateTime == null) {
                predicate = cb.and(predicate, cb.greaterThanOrEqualTo(root.get("dateTime"), startDateTime));
            }

            if (startDateTime == null && endDateTime != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("dateTime"), endDateTime));
            }

            if (currentDate != null) {
                predicate = cb.and(predicate, cb.lessThanOrEqualTo(root.get("dateTime"), currentDate));
                predicate = cb.and(predicate, cb.greaterThan(root.get("endDate"), currentDate));
            }

            return predicate;
        };
    }

    public static Specification<Lesson> currentLessonParams(UUID tutorId, UUID studentId, OffsetDateTime currentDate, List<LessonStatus> statuses) {
        return lessonsByParams(tutorId, studentId, statuses, null, null, currentDate.plusMinutes(15));
    }
}
