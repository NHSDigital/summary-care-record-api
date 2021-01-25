package uk.nhs.adaptors.scr.controllers.validation.getscrid.sort;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = SortMethodParameterValidator.class)
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface SortMethodParameter {
    String message() default "Invalid sort method";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
