package br.com.aftersunrise.sentio.application.analysis.commands;

import br.com.aftersunrise.sentio.application.abstractions.interfaces.ICommand;
import br.com.aftersunrise.sentio.application.analysis.data.SentimentDashboardResponse;

public record GetSentimentMetricsCommand() implements ICommand<SentimentDashboardResponse> {
}
