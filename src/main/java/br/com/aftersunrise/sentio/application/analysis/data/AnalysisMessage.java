package br.com.aftersunrise.sentio.application.analysis.data;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Mensagem de análise")
public record AnalysisMessage(String id, String text) {
}
