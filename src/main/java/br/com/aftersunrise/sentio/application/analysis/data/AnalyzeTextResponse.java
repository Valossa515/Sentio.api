package br.com.aftersunrise.sentio.application.analysis.data;

import br.com.aftersunrise.sentio.domain.models.analysis.enums.SentimentType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
@Schema(description = "Resposta da análise de texto")
public record AnalyzeTextResponse(List<Item> results) {
    public record Item(
            @Schema(description = "Identificador único do item analisado", example = "1")
            String id,
            @Schema(description = "Texto original que foi analisado", example = "I love programming!")
            String originalText,
            @Schema(description = "Texto limpo após pré-processamento", example = "love programming")
            SentimentType sentimentType,
            @Schema(description = "Pontuação de confiança na análise", example = "0.95")
            Double confidenceScore,
            @Schema(description = "Timestamp da análise", example = "2024-06-01T12:00:00")
            LocalDateTime analysisTimestamp
    ) {}
}