package br.com.aftersunrise.sentio.application.analysis.adapters;

import br.com.aftersunrise.sentio.application.abstractions.interfaces.IAnalysisAdapter;
import br.com.aftersunrise.sentio.application.analysis.commands.AnalyzeTextCommand;
import br.com.aftersunrise.sentio.domain.models.analysis.AnalysisResult;
import br.com.aftersunrise.sentio.domain.models.analysis.enums.SentimentType;
import org.springframework.stereotype.Component;

@Component
public class AnalysisAdapterImpl implements IAnalysisAdapter {
    @Override
    public AnalysisResult toAnalysisResult(AnalyzeTextCommand command, SentimentType sentimentType, Double score) {
        if (command == null || sentimentType == null || score == null) {
            throw new IllegalArgumentException("Argumentos para criação do resultado da análise não podem ser nulos.");
        }

        var texts = command.texts();
        if (texts == null || texts.isEmpty() || texts.getFirst() == null || texts.getFirst().isBlank()) {
            throw new IllegalArgumentException("O AnalyzeTextCommand deve conter pelo menos um texto não vazio.");
        }

        AnalysisResult result = new AnalysisResult();
        result.setOriginalText(texts.getFirst());
        result.setSentimentType(sentimentType);
        result.setConfidenceScore(score);
        return result;
    }
}
