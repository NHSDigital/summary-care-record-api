package uk.nhs.adaptors.scr.controllers.validation.getscrid.count;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.scr.exceptions.BadRequestException;

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
        try {
            checkGetScrCountParam(count);
        } catch (BadRequestException exc) {
            setErrorMessage(context, exc.getMessage());
            return false;
        }

        return true;
    }

    private void checkGetScrCountParam(Integer count) {
        if (count != null && !SUPPORTED_COUNT.equals(count)) {
            throw new BadRequestException(String.format("Invalid value - %s in field '_count'", count));
        }
    }

    private void setErrorMessage(ConstraintValidatorContext cxt, String message) {
        cxt.disableDefaultConstraintViolation();
        cxt.buildConstraintViolationWithTemplate(message)
            .addConstraintViolation();
    }
}
