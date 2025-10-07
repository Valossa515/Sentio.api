package br.com.aftersunrise.sentio.domain.services.analysis;

import br.com.aftersunrise.sentio.application.abstractions.data.HandlerResponseWithResult;
import br.com.aftersunrise.sentio.application.abstractions.data.MessageResources;
import br.com.aftersunrise.sentio.application.abstractions.handlers.CommandHandlerBase;
import br.com.aftersunrise.sentio.application.abstractions.interfaces.IAnalysisAdapter;
import br.com.aftersunrise.sentio.application.abstractions.interfaces.IAnalyzeTextHandler;
import br.com.aftersunrise.sentio.application.analysis.commands.AnalyzeTextCommand;
import br.com.aftersunrise.sentio.application.analysis.data.AnalyzeTextResponse;
import br.com.aftersunrise.sentio.domain.models.analysis.AnalysisResult;
import br.com.aftersunrise.sentio.domain.models.analysis.enums.Sentiment;
import br.com.aftersunrise.sentio.infrastructure.repositories.analysis.AnalysisResultRepository;
import jakarta.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

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

        return CompletableFuture.supplyAsync(() -> {

            try {
                var iaResult = simularAnaliseDeIA(request.text());

                // 2. **Adapter**: Converter o comando e o resultado da IA para a nossa entidade de domínio.
                AnalysisResult analysisResult = adapter.toAnalysisResult(
                        request,
                        iaResult.sentiment(),
                        iaResult.score()
                );

                // 3. **Repository**: Persistir a entidade no banco de dados.
                AnalysisResult savedResult = repository.save(analysisResult);

                // 4. **Response**: Criar o DTO de resposta a partir da entidade salva.
                return success(new AnalyzeTextResponse(
                        savedResult.getId(),
                        savedResult.getOriginalText(),
                        savedResult.getSentiment(),
                        savedResult.getConfidenceScore(),
                        savedResult.getAnalysisTimestamp()
                ));

            } catch (Exception e) {
                logger.error("Erro ao analisar o texto: {}", e.getMessage(), e);
                return badRequest(e.getMessage(), MessageResources.get("error.creating.text.analysis"));
            }
        });

    }

    private SentimentResult simularAnaliseDeIA(String text) {
        if (text.toLowerCase().contains("ótimo") || text.toLowerCase().contains("adorei")) {
            return new SentimentResult(Sentiment.POSITIVE, 0.98);
        } else if (text.toLowerCase().contains("ruim") || text.toLowerCase().contains("odiei")) {
            return new SentimentResult(Sentiment.NEGATIVE, 0.95);
        }
        return new SentimentResult(Sentiment.NEUTRAL, 0.85);
    }

    // Um record simples para encapsular o resultado da nossa simulação de IA.
    private record SentimentResult(Sentiment sentiment, Double score) {}
}