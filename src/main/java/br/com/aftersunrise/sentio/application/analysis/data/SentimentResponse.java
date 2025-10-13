package br.com.aftersunrise.sentio.application.analysis.data;

import br.com.aftersunrise.sentio.domain.models.analysis.AnalysisResult;
import br.com.aftersunrise.sentio.domain.models.analysis.enums.SentimentType;

import java.time.LocalDateTime;

public record SentimentResponse(
        String originalText,
        SentimentType sentimentType,
        Double confidenceScore,
        LocalDateTime analysisTimestamp) {

    public static SentimentResponse fromEntity(AnalysisResult analysisResult) {
        return new SentimentResponse(
                analysisResult.getOriginalText(),
                analysisResult.getSentimentType(),
                analysisResult.getConfidenceScore(),
                analysisResult.getAnalysisTimestamp()
        );
    }
}
