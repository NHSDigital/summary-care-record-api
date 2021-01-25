package uk.nhs.adaptors.scr.controllers.validation.getscrid.nhsnumber;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.scr.exceptions.BadRequestException;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PatientIdParameterValidator implements ConstraintValidator<PatientIdParameter, String> {

    private static final String PATIENT_ID_PREFIX = "https://fhir.nhs.uk/Id/nhs-number|";

    @Override
    public void initialize(PatientIdParameter contactNumber) {
    }

    @Override
    public boolean isValid(String patientId, ConstraintValidatorContext context) {
        try {
            checkNhsNumber(patientId);
        } catch (BadRequestException exc) {
            setErrorMessage(context, exc.getMessage());
            return false;
        }

        return true;
    }

    private void checkNhsNumber(String patientId) {
        if (isNotEmpty(patientId) && patientId.startsWith(PATIENT_ID_PREFIX)) {
            String nhsNumber = patientId.replace(PATIENT_ID_PREFIX, "");
            if (isNotEmpty(nhsNumber)) {
                return;
            }
        }

        throw new BadRequestException(String.format("Invalid value - %s in field 'patient'", patientId));
    }

    private void setErrorMessage(ConstraintValidatorContext cxt, String message) {
        cxt.disableDefaultConstraintViolation();
        cxt.buildConstraintViolationWithTemplate(message)
            .addConstraintViolation();
    }
}
