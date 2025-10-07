package br.com.aftersunrise.sentio.application.analysis.validators.annotations;

import br.com.aftersunrise.sentio.application.analysis.validators.AnalysisTextValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;


@Documented
@Constraint(validatedBy = AnalysisTextValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAnalysisText {
    String message() default "Texto de análise inválido.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}