package br.com.aftersunrise.sentio.application.analysis.validators;

import br.com.aftersunrise.sentio.application.analysis.commands.AnalyzeTextCommand;
import br.com.aftersunrise.sentio.application.analysis.validators.annotations.ValidAnalysisText;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AnalysisTextValidator implements ConstraintValidator<ValidAnalysisText, AnalyzeTextCommand> {


    @Override
    public boolean isValid(AnalyzeTextCommand value, ConstraintValidatorContext context) {
        if (value == null) return false;

        boolean valid = true;

        // 1. Texto não nulo e não vazio
        if (value.text() == null || value.text().isBlank()) {
            context.buildConstraintViolationWithTemplate("O texto para análise é obrigatório")
                    .addPropertyNode("text").addConstraintViolation();
            valid = false;
        }
        if (!valid) {
            context.disableDefaultConstraintViolation();
        }
        return valid;
    }
}
