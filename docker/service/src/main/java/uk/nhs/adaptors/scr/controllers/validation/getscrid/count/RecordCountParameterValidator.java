package uk.nhs.adaptors.scr.controllers.validation.getscrid.count;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class RecordCountParameterValidator implements ConstraintValidator<RecordCountParameter, Integer> {

    private static final Integer SUPPORTED_COUNT = 1;

    @Override
    public void initialize(RecordCountParameter contactNumber) {
    }

    @Override
    public boolean isValid(Integer count, ConstraintValidatorContext context) {
        if (!checkGetScrCountParam(count)) {
            setErrorMessage(context, String.format("Invalid value - %s in field '_count'", count));
            return false;
        }

        return true;
    }

    private boolean checkGetScrCountParam(Integer count) {
        return count == null || SUPPORTED_COUNT.equals(count);
    }

    private void setErrorMessage(ConstraintValidatorContext cxt, String message) {
        cxt.disableDefaultConstraintViolation();
        cxt.buildConstraintViolationWithTemplate(message)
            .addConstraintViolation();
    }
}
