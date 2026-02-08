package com.mytutorplatform.lessonsservice.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TutorDashboardActionsSummary {
    private long missingNotesCount;
    private long studentsWithoutNextLessonCount;
}

