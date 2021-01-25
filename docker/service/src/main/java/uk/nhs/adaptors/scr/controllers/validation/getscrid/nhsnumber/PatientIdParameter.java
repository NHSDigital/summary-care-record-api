package uk.nhs.adaptors.scr.controllers.validation.getscrid.nhsnumber;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = PatientIdParameterValidator.class)
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface PatientIdParameter {
    String message() default "Invalid NHS Number";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
