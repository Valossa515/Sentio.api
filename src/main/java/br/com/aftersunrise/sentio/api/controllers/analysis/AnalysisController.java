package br.com.aftersunrise.sentio.api.controllers.analysis;

import br.com.aftersunrise.sentio.application.abstractions.interfaces.IAnalyzeTextHandler;
import br.com.aftersunrise.sentio.application.abstractions.interfaces.IResponseEntityConverter;
import br.com.aftersunrise.sentio.application.analysis.commands.AnalyzeTextCommand;
import br.com.aftersunrise.sentio.application.analysis.data.AnalyzeTextResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/text-analysis/v1")
@RequiredArgsConstructor
public class AnalysisController {

    private final IAnalyzeTextHandler analyzeTextHandler;
    private final IResponseEntityConverter responseEntityConverter;

    @PostMapping("/analyze")
    public CompletableFuture<ResponseEntity<AnalyzeTextResponse>> analyzeText(
            @RequestBody AnalyzeTextCommand request) {

        return analyzeTextHandler.execute(request)
                .thenApplyAsync(response -> responseEntityConverter.convert(response, true));
    }
}