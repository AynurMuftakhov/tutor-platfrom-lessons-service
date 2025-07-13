package com.mytutorplatform.lessonsservice.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility class for parsing answer strings into structured data.
 */
public class AnswerParser {

    private static final String ANSWER_SEPARATOR = "\\|";

    /**
     * Parses the answer string into a 2D list where:
     * - The outer list represents each gap
     * - The inner list contains all acceptable answers for that gap
     *
     * @param answer The answer string from the GrammarItem
     * @return A 2D list of acceptable answers for each gap
     */
    public static List<List<String>> parseAnswers(String answer) {
        if (answer == null || answer.isEmpty()) {
            return new ArrayList<>();
        }

        // Split the answer string by semicolons to get answers for each gap
        String[] gapAnswers = answer.split(";");
        List<List<String>> result = new ArrayList<>(gapAnswers.length);

        // For each gap, split by pipe to get all acceptable answers
        for (String gapAnswer : gapAnswers) {
            List<String> acceptableAnswers = Arrays.asList(gapAnswer.split(ANSWER_SEPARATOR));
            result.add(acceptableAnswers);
        }

        return result;
    }

    /**
     * Counts the number of gaps in a text by looking for placeholders like {{1}}, {{2}}, etc.
     *
     * @param text The text containing gap placeholders
     * @return The number of unique gaps found
     */
    public static int countGaps(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        // Pattern to match placeholders like {{1}}, {{2}}, etc.
        Pattern pattern = Pattern.compile("\\{\\{(\\d+)\\}\\}");
        Matcher matcher = pattern.matcher(text);

        // Use a set to count unique gap numbers
        java.util.Set<Integer> gapNumbers = new java.util.HashSet<>();
        while (matcher.find()) {
            int gapNumber = Integer.parseInt(matcher.group(1));
            gapNumbers.add(gapNumber);
        }

        return gapNumbers.size();
    }
}