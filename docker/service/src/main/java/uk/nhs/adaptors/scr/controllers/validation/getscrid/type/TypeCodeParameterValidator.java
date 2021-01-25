package uk.nhs.adaptors.scr.controllers.validation.getscrid.type;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.scr.exceptions.BadRequestException;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TypeCodeParameterValidator implements ConstraintValidator<TypeCodeParameter, String> {

    private static final String SUPPORTED_TYPE = "http://snomed.info/sct|196981000000101";

    @Override
    public void initialize(TypeCodeParameter contactNumber) {
    }

    @Override
    public boolean isValid(String type, ConstraintValidatorContext context) {
        try {
            checkGetScrTypeParam(type);
        } catch (BadRequestException exc) {
            setErrorMessage(context, exc.getMessage());
            return false;
        }

        return true;
    }

    private void checkGetScrTypeParam(String type) {
        if (isNotEmpty(type) && !SUPPORTED_TYPE.equals(type)) {
            throw new BadRequestException(String.format("Invalid value - %s in field 'type'", type));
        }
    }

    private void setErrorMessage(ConstraintValidatorContext cxt, String message) {
        cxt.disableDefaultConstraintViolation();
        cxt.buildConstraintViolationWithTemplate(message)
            .addConstraintViolation();
    }
}
