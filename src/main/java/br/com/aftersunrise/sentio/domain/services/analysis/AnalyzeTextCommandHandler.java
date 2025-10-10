package br.com.aftersunrise.sentio.domain.services.analysis;

import br.com.aftersunrise.sentio.application.abstractions.data.HandlerResponseWithResult;
import br.com.aftersunrise.sentio.application.abstractions.handlers.CommandHandlerBase;
import br.com.aftersunrise.sentio.application.abstractions.interfaces.IAnalyzeTextHandler;
import br.com.aftersunrise.sentio.application.analysis.commands.AnalyzeTextCommand;
import br.com.aftersunrise.sentio.application.analysis.data.AnalysisMessage;
import br.com.aftersunrise.sentio.application.analysis.data.AnalyzeTextResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Service
public class AnalyzeTextCommandHandler extends CommandHandlerBase<AnalyzeTextCommand, AnalyzeTextResponse>
        implements IAnalyzeTextHandler {

    private static final Logger logger = LoggerFactory.getLogger(AnalyzeTextCommandHandler.class);
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private static final String TOPIC_ANALYSIS_REQUESTS = "text-analysis-requests";

    public AnalyzeTextCommandHandler(
            Validator validator,
            ObjectMapper objectMapper,
            KafkaTemplate<String, String> kafkaTemplate
    ) {
        super(logger, validator);
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    protected CompletableFuture<HandlerResponseWithResult<AnalyzeTextResponse>>
    doExecute(AnalyzeTextCommand request) {

        List<String> texts = getStrings(request);

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

    private List<String> getStrings(AnalyzeTextCommand request) {
        List<String> texts = request.texts();

        if (texts.isEmpty()) {
            return texts;
        }

        try {
            // Enviar como array quando houver múltiplos textos
            if (texts.size() > 1) {
                List<AnalysisMessage> messages = texts.stream()
                        .map(text -> new AnalysisMessage(UUID.randomUUID().toString(), text))
                        .toList();
                String batchJson = objectMapper.writeValueAsString(messages);
                kafkaTemplate.send(TOPIC_ANALYSIS_REQUESTS, batchJson);
            } else {
                // Enviar como objeto único quando há apenas um texto
                AnalysisMessage message = new AnalysisMessage(UUID.randomUUID().toString(), texts.get(0));
                String singleJson = objectMapper.writeValueAsString(message);
                kafkaTemplate.send(TOPIC_ANALYSIS_REQUESTS, singleJson);
            }

            logger.info("{} textos enviados para análise", texts.size());

        } catch (Exception e) {
            logger.error("Erro ao enviar mensagens para Kafka: {}", e.getMessage(), e);
        }

        return texts;
    }
}