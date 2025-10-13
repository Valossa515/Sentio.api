package br.com.aftersunrise.sentio.api.controllers.analysis;

import br.com.aftersunrise.sentio.application.abstractions.data.Message;
import br.com.aftersunrise.sentio.application.abstractions.interfaces.IAnalyzeTextHandler;
import br.com.aftersunrise.sentio.application.abstractions.interfaces.IListSentimentsQueryHandler;
import br.com.aftersunrise.sentio.application.abstractions.interfaces.IResponseEntityConverter;
import br.com.aftersunrise.sentio.application.abstractions.models.PagedResult;
import br.com.aftersunrise.sentio.application.abstractions.models.enums.SortDirection;
import br.com.aftersunrise.sentio.application.analysis.commands.AnalyzeTextCommand;
import br.com.aftersunrise.sentio.application.analysis.data.AnalyzeTextResponse;
import br.com.aftersunrise.sentio.application.analysis.data.ListSentimentsResponse;
import br.com.aftersunrise.sentio.application.analysis.data.SentimentResponse;
import br.com.aftersunrise.sentio.application.analysis.queries.ListSentimentsQuery;
import br.com.aftersunrise.sentio.application.analysis.queries.enums.SentimentOrderBy;
import br.com.aftersunrise.sentio.domain.models.analysis.enums.SentimentType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/text-analysis/v1")
@RequiredArgsConstructor
@Tag(name = "Text Analysis", description = "Operations related to text analysis.")
public class AnalysisController {

    private final IAnalyzeTextHandler analyzeTextHandler;
    private final IListSentimentsQueryHandler listSentimentsHandler;
    private final IResponseEntityConverter responseEntityConverter;

    @Operation(
            summary = "Analyze text for sentiment and entities",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Criado com sucesso",
                            content = @Content(schema = @Schema(implementation = AnalyzeTextResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Requisição inválida",
                            content = @Content(schema = @Schema(implementation = Message[].class))),
                    @ApiResponse(responseCode = "401", description = "Não autorizado",
                            content = @Content(schema = @Schema(implementation = Message[].class))),
                    @ApiResponse(responseCode = "403", description = "Acesso proibido",
                            content = @Content(schema = @Schema(implementation = Message[].class))),
                    @ApiResponse(responseCode = "500", description = "Erro interno",
                            content = @Content(schema = @Schema(implementation = Message[].class)))
            }
    )
    @PostMapping("/analyze")
    public CompletableFuture<ResponseEntity<AnalyzeTextResponse>> analyzeText(
            @RequestBody AnalyzeTextCommand request) {

        return analyzeTextHandler.execute(request)
                .thenApplyAsync(response -> responseEntityConverter.convert(response, true));
    }

    @Operation(
            summary = "List all detected sentiments",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Criado com sucesso",
                            content = @Content(schema = @Schema(implementation = ListSentimentsResponse.class))),
                    @ApiResponse(responseCode = "400", description = "Requisição inválida",
                            content = @Content(schema = @Schema(implementation = Message[].class))),
                    @ApiResponse(responseCode = "401", description = "Não autorizado",
                            content = @Content(schema = @Schema(implementation = Message[].class))),
                    @ApiResponse(responseCode = "403", description = "Acesso proibido",
                            content = @Content(schema = @Schema(implementation = Message[].class))),
                    @ApiResponse(responseCode = "500", description = "Erro interno",
                            content = @Content(schema = @Schema(implementation = Message[].class)))
            }
    )
    @GetMapping
    public CompletableFuture<ResponseEntity<PagedResult<SentimentResponse>>> listSentiments(
            @RequestParam(required = false) SentimentType sentimentType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "DESC") SortDirection sort,
            @RequestParam(required = false) SentimentOrderBy orderBy
    ) {
        var query = new ListSentimentsQuery();
        query.setSentimentType(sentimentType);
        query.setPage(page - 1);
        query.setSize(size);
        query.setSort(sort);
        query.setOrderBy(orderBy);

        return listSentimentsHandler.execute(query)
                .thenApplyAsync(response -> responseEntityConverter.convert(response, true));
    }
}