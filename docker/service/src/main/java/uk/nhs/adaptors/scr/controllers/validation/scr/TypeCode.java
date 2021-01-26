package uk.nhs.adaptors.scr.controllers.validation.scr;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = TypeCodeValidator.class)
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface TypeCode {
    String message() default "Invalid type code";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
