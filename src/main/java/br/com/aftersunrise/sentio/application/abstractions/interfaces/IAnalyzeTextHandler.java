package br.com.aftersunrise.sentio.application.abstractions.interfaces;

import br.com.aftersunrise.sentio.application.analysis.commands.AnalyzeTextCommand;
import br.com.aftersunrise.sentio.application.analysis.data.AnalyzeTextResponse;

public interface IAnalyzeTextHandler extends IHandler<AnalyzeTextCommand, AnalyzeTextResponse> {
}
