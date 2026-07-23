package io.github.fishstiz.cursors_extended.util;

import java.util.*;

public class KeywordSearcher<T> {
    public static final int EXACT_MATCH_SCORE = 0;
    public static final int PREFIX_MATCH_SCORE = 1;
    public static final int EXACT_WORD_MATCH_SCORE = 2;
    public static final int PREFIX_WORD_MATCH_SCORE = 3;
    public static final int SUBSTRING_MATCH_SCORE = 4;
    public static final int NO_MATCH_SCORE = Integer.MAX_VALUE;
    private final Map<T, Collection<String>> sourceKeywords;
    private boolean retainNonMatches = false;

    public KeywordSearcher() {
        this.sourceKeywords = new LinkedHashMap<>();
    }

    public void put(T source, Collection<String> keywords) {
        if (keywords.isEmpty()) {
            sourceKeywords.remove(source);
        } else {
            sourceKeywords.put(source, keywords);
        }
    }

    public KeywordSearcher<T> retainUnmatched(boolean retainNonMatches) {
        this.retainNonMatches = retainNonMatches;
        return this;
    }

    public KeywordSearcher<T> retainUnmatched() {
        return retainUnmatched(true);
    }

    public List<Result<T>> query(String query) {
        if (query.isEmpty()) return Collections.emptyList();

        query = query.toLowerCase(Locale.ROOT);

        List<Result<T>> results = new ArrayList<>();

        for (Map.Entry<T, Collection<String>> entry : sourceKeywords.entrySet()) {
            int score = NO_MATCH_SCORE;

            for (String keyword : entry.getValue()) {
                int index = keyword.toLowerCase(Locale.ROOT).indexOf(query);

                if (index != -1) {
                    int queryLen = query.length();
                    int keyLen = keyword.length();

                    if (index == 0 && keyLen == queryLen) {
                        score = EXACT_MATCH_SCORE;
                        break;
                    }

                    int endIndex = index + queryLen;
                    boolean isWordStart = (index == 0) || !Character.isLetterOrDigit(keyword.codePointBefore(index));
                    boolean isWordEnd = (endIndex == keyLen) || !Character.isLetterOrDigit(keyword.codePointAt(endIndex));
                    boolean isWordMatch = isWordStart && isWordEnd;

                    if (isWordMatch) {
                        score = Math.min(EXACT_WORD_MATCH_SCORE, score);
                    } else if (index == 0) {
                        score = PREFIX_MATCH_SCORE;
                    } else if (isWordStart) {
                        score = Math.min(PREFIX_WORD_MATCH_SCORE, score);
                    } else {
                        score = Math.min(SUBSTRING_MATCH_SCORE, score);
                    }
                }
            }

            if (score != NO_MATCH_SCORE || retainNonMatches) {
                results.add(new Result<>(entry.getKey(), score));
            }
        }

        return results;
    }

    public record Result<T>(T source, int score) implements Comparable<Result<T>> {
        @Override
        public int compareTo(KeywordSearcher.Result<T> o) {
            return Integer.compare(score, o.score);
        }
    }
}
