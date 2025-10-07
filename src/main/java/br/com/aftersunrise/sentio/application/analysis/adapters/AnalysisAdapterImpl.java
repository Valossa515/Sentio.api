package br.com.aftersunrise.sentio.application.analysis.adapters;

import br.com.aftersunrise.sentio.application.abstractions.interfaces.IAnalysisAdapter;
import br.com.aftersunrise.sentio.application.analysis.commands.AnalyzeTextCommand;
import br.com.aftersunrise.sentio.domain.models.analysis.AnalysisResult;
import br.com.aftersunrise.sentio.domain.models.analysis.enums.Sentiment;
import org.springframework.stereotype.Component;

@Component
public class AnalysisAdapterImpl implements IAnalysisAdapter {
    @Override
    public AnalysisResult toAnalysisResult(AnalyzeTextCommand command, Sentiment sentiment, Double score) {
        if (command == null || sentiment == null || score == null) {
            throw new IllegalArgumentException("Argumentos para criação do resultado da análise não podem ser nulos.");
        }

        AnalysisResult result = new AnalysisResult();
        result.setOriginalText(command.text());
        result.setSentiment(sentiment);
        result.setConfidenceScore(score);
        return result;
    }
}
