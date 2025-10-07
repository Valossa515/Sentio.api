package br.com.aftersunrise.sentio.application.analysis.data;

import br.com.aftersunrise.sentio.domain.models.analysis.enums.Sentiment;

import java.time.LocalDateTime;
import java.util.UUID;

public record AnalyzeTextResponse(
        String id,
        String originalText,
        Sentiment sentiment,
        Double confidenceScore,
        LocalDateTime analysisTimestamp
) {
}
