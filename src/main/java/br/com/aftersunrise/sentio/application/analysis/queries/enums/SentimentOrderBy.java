package br.com.aftersunrise.sentio.application.analysis.queries.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum SentimentOrderBy {
    ANALYSIS_TIMESTAMP("analysisTimestamp"),
    CONFIDENCE_SCORE("confidenceScore");

    private final String fieldName;

}
