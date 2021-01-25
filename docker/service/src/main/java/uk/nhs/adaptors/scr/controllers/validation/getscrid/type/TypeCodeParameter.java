package uk.nhs.adaptors.scr.controllers.validation.getscrid.type;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = TypeCodeParameterValidator.class)
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface TypeCodeParameter {
    String message() default "Invalid type code";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
