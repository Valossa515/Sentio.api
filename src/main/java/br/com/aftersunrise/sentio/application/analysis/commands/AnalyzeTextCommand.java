package br.com.aftersunrise.sentio.application.analysis.commands;

import br.com.aftersunrise.sentio.application.abstractions.interfaces.ICommand;
import br.com.aftersunrise.sentio.application.analysis.data.AnalyzeTextResponse;
import br.com.aftersunrise.sentio.application.analysis.validators.annotations.ValidAnalysisText;

@ValidAnalysisText
public record AnalyzeTextCommand(String text) implements ICommand<AnalyzeTextResponse> {
}
