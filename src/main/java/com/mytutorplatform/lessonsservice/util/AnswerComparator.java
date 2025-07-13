package com.mytutorplatform.lessonsservice.util;

/**
 * Utility class for comparing student answers with correct answers.
 * This class encapsulates the comparison logic to make it easy to extend in the future.
 */
public class AnswerComparator {

    /**
     * Compares a student's answer with a list of acceptable answers.
     * Current implementation: exact match after trim().toLowerCase()
     *
     * @param studentAnswer The student's answer
     * @param acceptableAnswers List of acceptable answers
     * @return true if the student's answer matches any of the acceptable answers
     */
    public static boolean isCorrect(String studentAnswer, java.util.List<String> acceptableAnswers) {
        if (studentAnswer == null || acceptableAnswers == null || acceptableAnswers.isEmpty()) {
            return false;
        }

        // Normalize the student's answer
        String normalizedStudentAnswer = normalize(studentAnswer);

        // Check if the normalized student answer matches any of the normalized acceptable answers
        return acceptableAnswers.stream()
                .map(AnswerComparator::normalize)
                .anyMatch(normalizedStudentAnswer::equals);
    }

    /**
     * Normalizes a string for comparison.
     * Current implementation: trim and convert to lowercase.
     * This method can be extended in the future to support more complex normalization.
     *
     * @param input The input string to normalize
     * @return The normalized string
     */
    public static String normalize(String input) {
        if (input == null) {
            return "";
        }
        return input.trim().toLowerCase();
    }
}