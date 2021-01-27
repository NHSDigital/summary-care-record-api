package uk.nhs.adaptors.scr.controllers.validation.scr;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = SortMethodValidator.class)
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface SortMethod {
    String message() default "Invalid sort method";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
