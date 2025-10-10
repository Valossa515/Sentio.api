package br.com.aftersunrise.sentio.messaging.consumers;

import br.com.aftersunrise.sentio.application.abstractions.interfaces.IAnalysisAdapter;
import br.com.aftersunrise.sentio.application.analysis.commands.AnalyzeTextCommand;
import br.com.aftersunrise.sentio.domain.models.analysis.AnalysisResult;
import br.com.aftersunrise.sentio.domain.models.analysis.enums.SentimentType;
import br.com.aftersunrise.sentio.infrastructure.repositories.analysis.AnalysisResultRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.language.v1.AnalyzeSentimentResponse;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.LanguageServiceClient;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Component
public class SentimentAnalysisConsumer {

    private static final Logger logger = LoggerFactory.getLogger(SentimentAnalysisConsumer.class);
    private static final String TOPIC_ANALYSIS_REQUESTS = "text-analysis-requests";
    private static final int CONCURRENCY = 8;

    private final LanguageServiceClient languageClient;
    private final AnalysisResultRepository repository;
    private final IAnalysisAdapter adapter;
    private final ObjectMapper objectMapper;
    private final Timer analysisTimer;
    private final MeterRegistry registry;

    public SentimentAnalysisConsumer(
            LanguageServiceClient languageClient,
            AnalysisResultRepository repository,
            IAnalysisAdapter adapter,
            ObjectMapper objectMapper,
            MeterRegistry registry
    ) {
        this.languageClient = languageClient;
        this.repository = repository;
        this.adapter = adapter;
        this.objectMapper = objectMapper;
        this.registry = registry;
        this.analysisTimer = Timer.builder("sentiment.analysis.duration")
                .description("Tempo total para processar análises de sentimento")
                .register(registry);
    }

    // Recebe mensagem JSON como String (cada evento do Kafka é uma mensagem JSON válida)
    @KafkaListener(topics = TOPIC_ANALYSIS_REQUESTS, groupId = "text-analysis-consumer")
    public void listenForTextAnalysis(String messageJson) {
        long start = System.nanoTime();

        processMessage(messageJson)
                .doOnSuccess(result -> {
                    long duration = System.nanoTime() - start;
                    analysisTimer.record(duration, TimeUnit.NANOSECONDS);
                    registry.counter("sentiment.analysis.batch.processed").increment();
                    logger.info("✅ Mensagem processada com sucesso em {}s", duration / 1_000_000_000.0);
                })
                .doOnError(ex -> {
                    registry.counter("sentiment.analysis.errors").increment();
                    logger.error("❌ Erro ao processar mensagem: {}", ex.getMessage(), ex);
                })
                .subscribe();
    }

    private Mono<AnalysisResult> processMessage(String messageJson) {
        return Mono.fromCallable(() -> {
                    if (messageJson == null || messageJson.isBlank()) {
                        registry.counter("sentiment.analysis.invalid_messages").increment();
                        return null;
                    }

                    List<String> texts = new ArrayList<>();
                    JsonNode node = objectMapper.readTree(messageJson);

                    if (node.isArray()) {
                        // Lista de objetos AnalysisMessage
                        for (JsonNode item : node) {
                            if (item.has("text")) {
                                texts.add(item.get("text").asText());
                            }
                        }
                    } else if (node.isObject() && node.has("text")) {
                        // Objeto único
                        texts.add(node.get("text").asText());
                    } else {
                        logger.warn("Mensagem em formato inesperado, ignorando: {}", messageJson);
                        registry.counter("sentiment.analysis.invalid_messages").increment();
                        return null;
                    }

                    if (texts.isEmpty()) {
                        registry.counter("sentiment.analysis.invalid_messages").increment();
                        return null;
                    }

                    List<AnalysisResult> results = new ArrayList<>();
                    for (String text : texts) {
                        Document doc = Document.newBuilder()
                                .setContent(text)
                                .setType(Document.Type.PLAIN_TEXT)
                                .build();

                        AnalyzeSentimentResponse response = languageClient.analyzeSentiment(doc);
                        var sentiment = response.getDocumentSentiment();
                        SentimentType sentimentType = mapSentiment(sentiment.getScore());
                        double score = sentiment.getScore();

                        var commandForAdapter = new AnalyzeTextCommand(List.of(text));
                        AnalysisResult result = adapter.toAnalysisResult(commandForAdapter, sentimentType, score);

                        repository.save(result);
                        logger.info("✔ Sentimento analisado e salvo: [{}] -> {}", sentimentType, text);
                        registry.counter("sentiment.analysis.success", "type", sentimentType.name()).increment();

                        results.add(result);
                    }

                    // Retorna o primeiro resultado apenas
                    return results.get(0);

                }).filter(Objects::nonNull)
                .subscribeOn(Schedulers.boundedElastic());
    }

    private SentimentType mapSentiment(float score) {
        if (score >= 0.25) return SentimentType.POSITIVE;
        if (score <= -0.10) return SentimentType.NEGATIVE;
        return SentimentType.NEUTRAL;
    }
}
