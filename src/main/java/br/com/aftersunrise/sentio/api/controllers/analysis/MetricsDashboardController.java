package br.com.aftersunrise.sentio.api.controllers.analysis;

import br.com.aftersunrise.sentio.application.abstractions.interfaces.IGetSentimentMetricsHandler;
import br.com.aftersunrise.sentio.application.abstractions.interfaces.IResponseEntityConverter;
import br.com.aftersunrise.sentio.application.analysis.commands.GetSentimentMetricsCommand;
import br.com.aftersunrise.sentio.application.analysis.data.SentimentDashboardResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/metrics/v1")
@RequiredArgsConstructor
@Tag(name = "Metrics", description = "Expose structured metrics for dashboards.")
public class MetricsDashboardController {

    private final IGetSentimentMetricsHandler dashboardHandler;
    private final IResponseEntityConverter responseEntityConverter;

    @GetMapping("/dashboard")
    @Operation(summary = "Returns metrics ready for chart visualization")
    public CompletableFuture<ResponseEntity<SentimentDashboardResponse>> getDashboard() {
        return dashboardHandler.execute(new GetSentimentMetricsCommand())
                .thenApplyAsync(r -> responseEntityConverter.convert(r, true));
    }
}
