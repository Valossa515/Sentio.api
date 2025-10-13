package br.com.aftersunrise.sentio.application.analysis.data;

import java.util.List;

public record SentimentDashboardResponse(
        long totalProcessed,
        long totalSuccess,
        long totalFailures,
        long totalErrors,
        long totalInvalid,
        DurationStats durationStats,
        List<SentimentBreakdownItem> sentimentDistribution,
        List<TimeSeriesPoint> timeSeries) {

    public record DurationStats(
            double meanSeconds,
            double maxSeconds,
            double totalSeconds,
            long count
    ) {}

    public record SentimentBreakdownItem(
            String sentiment,
            double count
    ) {}

    public record TimeSeriesPoint(
            String timestamp,
            long processed,
            long success,
            long failures
    ) {}
}