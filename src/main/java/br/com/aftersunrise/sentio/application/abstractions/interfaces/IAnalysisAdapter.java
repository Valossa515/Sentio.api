package br.com.aftersunrise.sentio.application.abstractions.interfaces;

import br.com.aftersunrise.sentio.application.analysis.commands.AnalyzeTextCommand;
import br.com.aftersunrise.sentio.domain.models.analysis.AnalysisResult;
import br.com.aftersunrise.sentio.domain.models.analysis.enums.Sentiment;

public interface IAnalysisAdapter {
    AnalysisResult toAnalysisResult(AnalyzeTextCommand command, Sentiment sentiment, Double score);
}
