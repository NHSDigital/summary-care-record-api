package uk.nhs.adaptors.scr.controllers.validation.scr;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class PatientIdValidator implements ConstraintValidator<PatientId, String> {

    private static final String PATIENT_ID_PREFIX = "https://fhir.nhs.uk/Id/nhs-number|";

    @Override
    public boolean isValid(String patientId, ConstraintValidatorContext context) {
        if (!isNhsNumber(patientId)) {
            setErrorMessage(context, String.format("Invalid value - %s in field 'patient'", patientId));
            return false;
        }

        return true;
    }

    private boolean isNhsNumber(String patientId) {
        if (isNotEmpty(patientId) && patientId.startsWith(PATIENT_ID_PREFIX)) {
            String nhsNumber = patientId.replace(PATIENT_ID_PREFIX, "");
            return isNotEmpty(nhsNumber);
        }

        return false;
    }

    private void setErrorMessage(ConstraintValidatorContext cxt, String message) {
        cxt.disableDefaultConstraintViolation();
        cxt.buildConstraintViolationWithTemplate(message)
            .addConstraintViolation();
    }
}
