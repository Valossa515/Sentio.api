package br.com.aftersunrise.sentio.domain.services.analysis;

import br.com.aftersunrise.sentio.application.abstractions.data.HandlerResponseWithResult;
import br.com.aftersunrise.sentio.application.abstractions.handlers.QueryHandlerBase;
import br.com.aftersunrise.sentio.application.abstractions.interfaces.IListSentimentsQueryHandler;
import br.com.aftersunrise.sentio.application.abstractions.models.PagedResult;
import br.com.aftersunrise.sentio.application.abstractions.models.enums.SortDirection;
import br.com.aftersunrise.sentio.application.analysis.data.SentimentResponse;
import br.com.aftersunrise.sentio.application.analysis.queries.ListSentimentsQuery;
import br.com.aftersunrise.sentio.domain.models.analysis.AnalysisResult;
import br.com.aftersunrise.sentio.infrastructure.repositories.analysis.AnalysisResultRepository;
import jakarta.validation.Validator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class GetSentimentsQueryHandler extends QueryHandlerBase<ListSentimentsQuery, PagedResult<SentimentResponse>>
        implements IListSentimentsQueryHandler {

    private final AnalysisResultRepository repository;

    public GetSentimentsQueryHandler(Validator validator, AnalysisResultRepository repository) {
        super(log, validator);
        this.repository = repository;
    }

    @Override
    protected CompletableFuture<HandlerResponseWithResult<PagedResult<SentimentResponse>>> doExecute(ListSentimentsQuery request) {
        try {
            var sentimentType = request.getSentimentType();
            var sortField = request.getOrderBy() != null
                    ? request.getOrderBy().getFieldName()
                    : "analysisTimestamp";

            var direction = request.getSort() == SortDirection.ASC
                    ? Sort.Direction.ASC
                    : Sort.Direction.DESC;

            var pageable = PageRequest.of(request.getPage(), request.getSize(), Sort.by(direction, sortField));

            Page<AnalysisResult> pageResult = sentimentType != null
                    ? repository.findBySentimentType(sentimentType, pageable)
                    : repository.findAll(pageable);

            var data = pageResult.getContent().stream()
                    .map(SentimentResponse::fromEntity)
                    .toList();

            var result = new PagedResult<>(
                    data,
                    pageResult.getTotalElements(),
                    pageResult.getNumber() + 1,
                    pageResult.getSize()
            );

            return CompletableFuture.completedFuture(success(result));
        } catch (Exception e) {
            log.error("Error fetching sentiments", e);
            return CompletableFuture.completedFuture(
                    internalServerError("Error fetching sentiments", e.getMessage())
            );
        }
    }
}