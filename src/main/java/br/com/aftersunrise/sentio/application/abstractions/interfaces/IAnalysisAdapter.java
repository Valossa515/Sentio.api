package br.com.aftersunrise.sentio.application.abstractions.interfaces;

import br.com.aftersunrise.sentio.application.analysis.commands.AnalyzeTextCommand;
import br.com.aftersunrise.sentio.domain.models.analysis.AnalysisResult;
import br.com.aftersunrise.sentio.domain.models.analysis.enums.SentimentType;

public interface IAnalysisAdapter {
    AnalysisResult toAnalysisResult(AnalyzeTextCommand command, SentimentType sentimentType, Double score);
}
