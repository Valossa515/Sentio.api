package br.com.aftersunrise.sentio.domain.services.analysis;

import br.com.aftersunrise.sentio.application.abstractions.data.HandlerResponseWithResult;
import br.com.aftersunrise.sentio.application.abstractions.handlers.CommandHandlerBase;
import br.com.aftersunrise.sentio.application.abstractions.interfaces.IAnalyzeTextHandler;
import br.com.aftersunrise.sentio.application.analysis.commands.AnalyzeTextCommand;
import br.com.aftersunrise.sentio.application.analysis.data.AnalyzeTextResponse;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class AnalyzeTextCommandHandler extends CommandHandlerBase<AnalyzeTextCommand, AnalyzeTextResponse>
        implements IAnalyzeTextHandler {

    private static final Logger logger = LoggerFactory.getLogger(AnalyzeTextCommandHandler.class);
    private final KafkaTemplate<String, String> kafkaTemplate;
    private static final String TOPIC_ANALYSIS_REQUESTS = "text-analysis-requests";

    public AnalyzeTextCommandHandler(
            Validator validator,
            KafkaTemplate<String, String> kafkaTemplate
    ) {
        super(logger, validator);
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    protected CompletableFuture<HandlerResponseWithResult<AnalyzeTextResponse>>
    doExecute(AnalyzeTextCommand request) {

        List<String> texts = request.texts();

        texts.forEach(text -> {
            logger.info("Enviando texto para análise no tópico Kafka: {}", text);
            kafkaTemplate.send(TOPIC_ANALYSIS_REQUESTS, text, text);
        });

        AnalyzeTextResponse.Item acceptedItem = new AnalyzeTextResponse.Item(
                null,
                String.format("%d textos aceitos para processamento assíncrono.", texts.size()),
                null,
                null,
                null
        );

        AnalyzeTextResponse response = new AnalyzeTextResponse(Collections.singletonList(acceptedItem));

        return CompletableFuture.completedFuture(
                success(response)
        );
    }
}