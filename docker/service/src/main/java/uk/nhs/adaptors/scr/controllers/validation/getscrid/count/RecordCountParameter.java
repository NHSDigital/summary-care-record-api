package uk.nhs.adaptors.scr.controllers.validation.getscrid.count;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = RecordCountParameterValidator.class)
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface RecordCountParameter {
    String message() default "Invalid Count Value";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
