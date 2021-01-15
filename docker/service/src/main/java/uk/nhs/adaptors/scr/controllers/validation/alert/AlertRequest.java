package uk.nhs.adaptors.scr.controllers.validation.alert;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Constraint(validatedBy = AlertRequestValidator.class)
@Target(PARAMETER)
@Retention(RUNTIME)
public @interface AlertRequest {
    String message() default "Invalid ALERT request";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
