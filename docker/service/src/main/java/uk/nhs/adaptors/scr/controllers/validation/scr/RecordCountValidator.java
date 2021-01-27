package uk.nhs.adaptors.scr.controllers.validation.scr;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RecordCountValidator implements ConstraintValidator<RecordCount, Integer> {

    private static final Integer SUPPORTED_COUNT = 1;

    @Override
    public void initialize(RecordCount contactNumber) {
    }

    @Override
    public boolean isValid(Integer count, ConstraintValidatorContext context) {
        if (!isScrCount(count)) {
            setErrorMessage(context, String.format("Invalid value - %s in field '_count'", count));
            return false;
        }

        return true;
    }

    private boolean isScrCount(Integer count) {
        return count == null || SUPPORTED_COUNT.equals(count);
    }

    private void setErrorMessage(ConstraintValidatorContext cxt, String message) {
        cxt.disableDefaultConstraintViolation();
        cxt.buildConstraintViolationWithTemplate(message)
            .addConstraintViolation();
    }
}
