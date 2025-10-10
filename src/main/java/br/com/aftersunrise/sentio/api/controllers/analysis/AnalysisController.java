package br.com.aftersunrise.sentio.api.controllers.analysis;

import br.com.aftersunrise.sentio.application.abstractions.data.Message;
import br.com.aftersunrise.sentio.application.abstractions.interfaces.IAnalyzeTextHandler;
import br.com.aftersunrise.sentio.application.abstractions.interfaces.IResponseEntityConverter;
import br.com.aftersunrise.sentio.application.analysis.commands.AnalyzeTextCommand;
import br.com.aftersunrise.sentio.application.analysis.data.AnalyzeTextResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Text Analysis", description = "Operations related to text analysis.")
public class AnalysisController {

    private final IAnalyzeTextHandler analyzeTextHandler;
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
}