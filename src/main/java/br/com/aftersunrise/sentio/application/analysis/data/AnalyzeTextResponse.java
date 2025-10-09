package br.com.aftersunrise.sentio.application.analysis.data;

import br.com.aftersunrise.sentio.domain.models.analysis.enums.SentimentType;

import java.time.LocalDateTime;
import java.util.List;

public record AnalyzeTextResponse(List<Item> results) {
    public record Item(
            String id,
            String originalText,
            SentimentType sentimentType,
            Double confidenceScore,
            LocalDateTime analysisTimestamp
    ) {}
}