package uk.nhs.adaptors.scr.controllers.validation.getscrid;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = GetScrIdRequestValidator.class)
@Target(METHOD)
@Retention(RUNTIME)
public @interface GetScrIdRequest {
    String message() default "Invalid GET SCR ID request";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
