package br.com.aftersunrise.sentio.application.analysis.commands;

import br.com.aftersunrise.sentio.application.abstractions.interfaces.ICommand;
import br.com.aftersunrise.sentio.application.analysis.data.AnalyzeTextResponse;
import br.com.aftersunrise.sentio.application.analysis.validators.annotations.ValidAnalysisText;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Collections;
import java.util.List;

@ValidAnalysisText
@Schema(description = "Comando para análise de texto")
public record AnalyzeTextCommand(List<String> texts) implements ICommand<AnalyzeTextResponse> {
    @JsonCreator
    public AnalyzeTextCommand(
            @Schema(description = "Texto a ser analisado", example = "Bom produto", required = true)
            @JsonProperty("text") String text,
            @Schema(description = "Lista de textos a serem analisados", example = "[\"Bom produto\", \"Chegou atrasado\"]", required = true)
            @JsonProperty("texts") List<String> texts
    ) {
        this(texts != null
                ? texts
                : (text != null ? Collections.singletonList(text) : Collections.emptyList()));
    }
}
