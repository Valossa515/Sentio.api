package br.com.aftersunrise.sentio.domain.services.analysis;

import br.com.aftersunrise.sentio.application.abstractions.data.HandlerResponseWithResult;
import br.com.aftersunrise.sentio.application.abstractions.data.MessageResources;
import br.com.aftersunrise.sentio.application.abstractions.handlers.CommandHandlerBase;
import br.com.aftersunrise.sentio.application.abstractions.interfaces.IAnalysisAdapter;
import br.com.aftersunrise.sentio.application.abstractions.interfaces.IAnalyzeTextHandler;
import br.com.aftersunrise.sentio.application.analysis.commands.AnalyzeTextCommand;
import br.com.aftersunrise.sentio.application.analysis.data.AnalyzeTextResponse;
import br.com.aftersunrise.sentio.domain.models.analysis.enums.SentimentType;
import br.com.aftersunrise.sentio.infrastructure.repositories.analysis.AnalysisResultRepository;
import com.google.cloud.language.v1.AnalyzeSentimentResponse;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.LanguageServiceClient;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class AnalyzeTextCommandHandler extends CommandHandlerBase<AnalyzeTextCommand, AnalyzeTextResponse>
    implements IAnalyzeTextHandler {

    private static final Logger logger = LoggerFactory.getLogger(AnalyzeTextCommandHandler.class);
    private final AnalysisResultRepository repository;
    private final IAnalysisAdapter adapter;

    public AnalyzeTextCommandHandler(
        Validator validator,
        AnalysisResultRepository repository,
        IAnalysisAdapter adapter) {
        super(logger, validator);
        this.repository = repository;
        this.adapter = adapter;
    }

    @Override
    protected CompletableFuture<HandlerResponseWithResult<AnalyzeTextResponse>>
        doExecute(AnalyzeTextCommand request) {

        return Flux.fromIterable(request.texts())
                .flatMap(this::analyzeSentimentAsync)
                .map(result -> adapter.toAnalysisResult(
                        new AnalyzeTextCommand(List.of(result.text())),
                        result.sentiment(),
                        result.score()))
                .collectList()
                .flatMapMany(results -> Flux.fromIterable(repository.saveAll(results))) // salva todos
                .map(e -> new AnalyzeTextResponse.Item(
                        e.getId(),
                        e.getOriginalText(),
                        e.getSentimentType(),
                        e.getConfidenceScore(),
                        e.getAnalysisTimestamp()))
                .collectList()
                .map(AnalyzeTextResponse::new)
                .map(this::success)
                .onErrorResume(ex -> {
                    logger.error("Erro durante a análise reativa: {}", ex.getMessage(), ex);
                    return Mono.just(badRequest(ex.getMessage(), MessageResources.get("error.creating.text.analysis")));
                })
                .toFuture();
    }

    private Mono<SentimentResult> analyzeSentimentAsync(String text) {
        return Mono.fromCallable(() -> {
            try (LanguageServiceClient language = LanguageServiceClient.create()) {
                Document doc = Document.newBuilder()
                        .setContent(text)
                        .setType(Document.Type.PLAIN_TEXT)
                        .build();

                AnalyzeSentimentResponse response = language.analyzeSentiment(doc);
                var sentiment = response.getDocumentSentiment();

                return new SentimentResult(
                        text,
                        mapSentiment(sentiment.getScore()),
                        (double) sentiment.getScore()
                );
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }


    private SentimentType mapSentiment(float score) {
        if (score >= 0.25) return SentimentType.POSITIVE;
        if (score <= -0.25) return SentimentType.NEGATIVE;
        return SentimentType.NEUTRAL;
    }

    private record SentimentResult(String text, SentimentType sentiment, Double score) {}
}