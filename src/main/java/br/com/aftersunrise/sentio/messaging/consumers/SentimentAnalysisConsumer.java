package br.com.aftersunrise.sentio.messaging.consumers;

import br.com.aftersunrise.sentio.application.abstractions.interfaces.IAnalysisAdapter;
import br.com.aftersunrise.sentio.application.analysis.commands.AnalyzeTextCommand;
import br.com.aftersunrise.sentio.domain.models.analysis.AnalysisResult;
import br.com.aftersunrise.sentio.domain.models.analysis.enums.SentimentType;
import br.com.aftersunrise.sentio.infrastructure.repositories.analysis.AnalysisResultRepository;
import com.google.cloud.language.v1.AnalyzeSentimentResponse;
import com.google.cloud.language.v1.Document;
import com.google.cloud.language.v1.LanguageServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.util.List;

@Component
public class SentimentAnalysisConsumer {

    private static final Logger logger = LoggerFactory.getLogger(SentimentAnalysisConsumer.class);
    private static final String TOPIC_ANALYSIS_REQUESTS = "text-analysis-requests";

    private final AnalysisResultRepository repository;
    private final IAnalysisAdapter adapter;

    // A injeção do LanguageServiceClient é indireta, ele será criado dentro do Mono.fromCallable
    // para garantir que a resource (o cliente gRPC) seja gerenciada corretamente
    public SentimentAnalysisConsumer(AnalysisResultRepository repository, IAnalysisAdapter adapter) {
        this.repository = repository;
        this.adapter = adapter;
    }

    /**
     * Consumidor Kafka. O 'groupId' deve ser o mesmo configurado no application.yml.
     */
    @KafkaListener(topics = TOPIC_ANALYSIS_REQUESTS, groupId = "text-analysis-consumer")
    public void listenForTextAnalysis(String text) {
        // Envolve a lógica em um Mono para usar o Schedulers.boundedElastic
        // e liberar a thread do Consumer para processar a próxima mensagem rapidamente.
        analyzeSentimentAndSave(text)
                .subscribe(
                        result -> logger.info("Análise concluída e salva para o texto: {}", text),
                        error -> logger.error("Erro CRÍTICO ao processar e salvar a análise para o texto {}: {}",
                                text, error.getMessage(), error)
                );
    }

    /**
     * Lógica assíncrona para analisar o sentimento e persistir o resultado.
     */
    private Mono<AnalysisResult> analyzeSentimentAndSave(String text) {
        return Mono.fromCallable(() -> {
            logger.info("Iniciando análise de sentimento para: '{}'", text);

            // 1. ANÁLISE DE SENTIMENTO
            SentimentResult result = analyzeSentiment(text);

            // 2. MAPEAMENTO
            // Cria um command 'dummy' para satisfazer a interface do IAnalysisAdapter
            var commandForAdapter = new AnalyzeTextCommand(List.of(text));
            var analysisResult = adapter.toAnalysisResult(
                    commandForAdapter,
                    result.sentiment(),
                    result.score()
            );

            // 3. PERSISTÊNCIA (Assumindo que o repository.save é síncrono ou block)
            AnalysisResult savedResult = repository.save(analysisResult);
            logger.info("Análise salva com sucesso. Sentimento: {}", savedResult.getSentimentType());

            return savedResult;
        }).subscribeOn(Schedulers.boundedElastic()); // Usa um pool de threads I/O para o blocking call
    }


    // ===================================
    // LÓGICA DE ANÁLISE DE SENTIMENTO MOVIDA
    // ===================================

    private SentimentResult analyzeSentiment(String text) throws IOException {
        // Usamos try-with-resources para garantir que o cliente seja fechado
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
    }


    private SentimentType mapSentiment(float score) {
        if (score >= 0.25) return SentimentType.POSITIVE;
        if (score <= -0.10) return SentimentType.NEGATIVE;
        return SentimentType.NEUTRAL;
    }

    private record SentimentResult(String text, SentimentType sentiment, Double score) {}
}
