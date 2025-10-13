package br.com.aftersunrise.sentio.application.abstractions.interfaces;

import br.com.aftersunrise.sentio.application.analysis.commands.GetSentimentMetricsCommand;
import br.com.aftersunrise.sentio.application.analysis.data.SentimentDashboardResponse;

public interface IGetSentimentMetricsHandler extends IHandler<GetSentimentMetricsCommand, SentimentDashboardResponse> {
}
