package uk.nhs.adaptors.scr.controllers.validation.acs;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = AcsRequestValidator.class)
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface AcsRequest {
    String message() default "Invalid ACS request";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
