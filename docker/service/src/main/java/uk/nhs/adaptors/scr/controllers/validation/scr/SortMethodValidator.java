package uk.nhs.adaptors.scr.controllers.validation.scr;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static org.apache.commons.lang3.StringUtils.isEmpty;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SortMethodValidator implements ConstraintValidator<SortMethod, String> {

    private static final String SUPPORTED_SORT = "date";

    @Override
    public void initialize(SortMethod contactNumber) {
    }

    @Override
    public boolean isValid(String sort, ConstraintValidatorContext context) {
        if (!isScrSort(sort)) {
            setErrorMessage(context, String.format("Invalid value - %s in field '_sort'", sort));
            return false;
        }

        return true;
    }

    private boolean isScrSort(String sort) {
        return isEmpty(sort) || SUPPORTED_SORT.equals(sort);
    }

    private void setErrorMessage(ConstraintValidatorContext cxt, String message) {
        cxt.disableDefaultConstraintViolation();
        cxt.buildConstraintViolationWithTemplate(message)
            .addConstraintViolation();
    }
}
