package br.com.aftersunrise.sentio.domain.services.analysis;

import br.com.aftersunrise.sentio.application.abstractions.data.HandlerResponseWithResult;
import br.com.aftersunrise.sentio.application.abstractions.handlers.CommandHandlerBase;
import br.com.aftersunrise.sentio.application.abstractions.interfaces.IGetSentimentMetricsHandler;
import br.com.aftersunrise.sentio.application.analysis.commands.GetSentimentMetricsCommand;
import br.com.aftersunrise.sentio.application.analysis.data.SentimentDashboardResponse;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class GetSentimentDashboardHandler
        extends CommandHandlerBase<GetSentimentMetricsCommand, SentimentDashboardResponse>
        implements IGetSentimentMetricsHandler {

    private final MeterRegistry registry;

    public GetSentimentDashboardHandler(Validator validator, MeterRegistry registry) {
        super(log, validator);
        this.registry = registry;
    }

    @Override
    protected CompletableFuture<HandlerResponseWithResult<SentimentDashboardResponse>> doExecute(GetSentimentMetricsCommand request) {
        try {
            log.info("🔍 Coletando métricas de sentimento...");

            registry.getMeters().forEach(m -> log.info(
                    "Métrica registrada -> name={}, tags={}, tipo={}",
                    m.getId().getName(),
                    m.getId().getTags(),
                    m.getId().getType()
            ));

            long processed = Math.round(getCounterSum("sentiment.analysis.batch.processed"));
            long success = Math.round(getCounterSum("sentiment.analysis.success"));
            long failures = Math.round(getCounterSum("sentiment.analysis.failures"));
            long errors = Math.round(getCounterSum("sentiment.analysis.errors"));
            long invalid = Math.round(getCounterSum("sentiment.analysis.invalid_messages"));

            Timer timer = registry.find("sentiment.analysis.duration").timer();
            SentimentDashboardResponse.DurationStats durationStats = null;
            if (timer != null) {
                durationStats = new SentimentDashboardResponse.DurationStats(
                        timer.mean(TimeUnit.SECONDS),
                        timer.max(TimeUnit.SECONDS),
                        timer.totalTime(TimeUnit.SECONDS),
                        timer.count()
                );
            }

            List<SentimentDashboardResponse.SentimentBreakdownItem> breakdown = new ArrayList<>();
            for (Meter meter : registry.find("sentiment.analysis.success").meters()) {
                meter.measure().forEach(measure -> {
                    String type = meter.getId().getTag("type");
                    if (type != null) {
                        breakdown.add(new SentimentDashboardResponse.SentimentBreakdownItem(
                                type,
                                measure.getValue()
                        ));
                    }
                });
            }

            List<SentimentDashboardResponse.TimeSeriesPoint> timeSeries = List.of(
                    new SentimentDashboardResponse.TimeSeriesPoint(
                            DateTimeFormatter.ISO_INSTANT.format(Instant.now()),
                            processed, success, failures
                    )
            );

            SentimentDashboardResponse response = new SentimentDashboardResponse(
                    processed,
                    success,
                    failures,
                    errors,
                    invalid,
                    durationStats,
                    breakdown,
                    timeSeries
            );

            log.info("✅ Métricas consolidadas com sucesso: {}", response);
            return CompletableFuture.completedFuture(success(response));

        } catch (Exception e) {
            log.error("❌ Erro ao gerar métricas para dashboard: {}", e.getMessage(), e);
            return CompletableFuture.completedFuture(
                    badRequest("Erro ao gerar métricas para dashboard.", e.getMessage())
            );
        }
    }

    private double getCounterSum(String name) {
        return registry.find(name)
                .counters()
                .stream()
                .mapToDouble(Counter::count)
                .sum();
    }
}
