package br.com.aftersunrise.sentio.application.analysis.commands;

import br.com.aftersunrise.sentio.application.abstractions.interfaces.ICommand;
import br.com.aftersunrise.sentio.application.analysis.data.AnalyzeTextResponse;
import br.com.aftersunrise.sentio.application.analysis.validators.annotations.ValidAnalysisText;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collections;
import java.util.List;

@ValidAnalysisText
public record AnalyzeTextCommand(List<String> texts) implements ICommand<AnalyzeTextResponse> {
    @JsonCreator
    public AnalyzeTextCommand(
            @JsonProperty("text") String text,
            @JsonProperty("texts") List<String> texts
    ) {
        this(texts != null
                ? texts
                : (text != null ? Collections.singletonList(text) : Collections.emptyList()));
    }
}
