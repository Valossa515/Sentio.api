package br.com.aftersunrise.sentio.application.analysis.validators;

import br.com.aftersunrise.sentio.application.analysis.commands.AnalyzeTextCommand;
import br.com.aftersunrise.sentio.application.analysis.validators.annotations.ValidAnalysisText;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;

public class AnalysisTextValidator implements ConstraintValidator<ValidAnalysisText, AnalyzeTextCommand> {

    @Override
    public boolean isValid(AnalyzeTextCommand value, ConstraintValidatorContext context) {
        if (value == null) return false;

        List<String> texts = value.texts();
        if (texts == null || texts.isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("A lista de textos para análise não pode estar vazia.")
                    .addPropertyNode("texts")
                    .addConstraintViolation();
            return false;
        }

        boolean hasInvalid = texts.stream().anyMatch(t -> t == null || t.isBlank());
        if (hasInvalid) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("Todos os textos devem ser preenchidos corretamente.")
                    .addPropertyNode("texts")
                    .addConstraintViolation();
            return false;
        }

        return true;
    }
}
