package uk.nhs.adaptors.scr.controllers.validation.getscrid.sort;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.scr.exceptions.BadRequestException;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class SortMethodParameterValidator implements ConstraintValidator<SortMethodParameter, String> {

    private static final String SUPPORTED_SORT = "date";

    @Override
    public void initialize(SortMethodParameter contactNumber) {
    }

    @Override
    public boolean isValid(String sort, ConstraintValidatorContext context) {
        try {
            checkGetScrSortParam(sort);
        } catch (BadRequestException exc) {
            setErrorMessage(context, exc.getMessage());
            return false;
        }

        return true;
    }

    private void checkGetScrSortParam(String sort) {
        if (isNotEmpty(sort) && !SUPPORTED_SORT.equals(sort)) {
            throw new BadRequestException(String.format("Invalid value - %s in field '_sort'", sort));
        }
    }

    private void setErrorMessage(ConstraintValidatorContext cxt, String message) {
        cxt.disableDefaultConstraintViolation();
        cxt.buildConstraintViolationWithTemplate(message)
            .addConstraintViolation();
    }
}
