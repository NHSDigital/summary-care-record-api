package uk.nhs.adaptors.scr.controllers.validation.getscrid;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.scr.exceptions.BadRequestException;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.constraintvalidation.SupportedValidationTarget;
import javax.validation.constraintvalidation.ValidationTarget;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@SupportedValidationTarget(ValidationTarget.PARAMETERS)
public class GetScrIdRequestValidator implements ConstraintValidator<GetScrIdRequest, Object[]> {

    private static final String PATIENT_ID_PREFIX = "https://fhir.nhs.uk/Id/nhs-number|";
    private static final String SUPPORTED_TYPE = "http://snomed.info/sct|196981000000101";
    private static final String SUPPORTED_SORT = "date";
    private static final Integer SUPPORTED_COUNT = 1;

    private static final int PATIENT_PARAMETER_POSITION = 3;
    private static final int TYPE_PARAMETER_POSITION = 4;
    private static final int SORT_PARAMETER_POSITION = 5;
    private static final int COUNT_PARAMETER_POSITION = 6;

    @Override
    public void initialize(GetScrIdRequest contactNumber) {
    }

    /*
     * @param parameters - parameters of the getScrId method in GetScrController.
     *                   The parameters are expected to be in the following order:
     *                   0 - String nhsdAsid
     *                   1 - String clientIp
     *                   2 - String clientRequestURL
     *                   3 - String patient
     *                   4 - String type
     *                   5 - String sort
     *                   6 - Integer count
     *                   7 - HttpServletRequest request
     */
    @Override
    public boolean isValid(Object[] parameters, ConstraintValidatorContext context) {
        try {
            String patient = (String) parameters[PATIENT_PARAMETER_POSITION];
            String type = (String) parameters[TYPE_PARAMETER_POSITION];
            String sort = (String) parameters[SORT_PARAMETER_POSITION];
            Integer count = (Integer) parameters[COUNT_PARAMETER_POSITION];

            checkAndExtractNhsNumber(patient);
            checkGetScrTypeParam(type);
            checkGetScrSortParam(sort);
            checkGetScrCountParam(count);
        } catch (BadRequestException exc) {
            setErrorMessage(context, exc.getMessage());
            return false;
        }

        return true;
    }

    private void checkAndExtractNhsNumber(String patientId) {
        if (isNotEmpty(patientId) && patientId.startsWith(PATIENT_ID_PREFIX)) {
            String nhsNumber = patientId.replace(PATIENT_ID_PREFIX, "");
            if (isNotEmpty(nhsNumber)) {
                return;
            }
        }

        throw new BadRequestException(String.format("Invalid value - %s in field 'patient'", patientId));
    }

    private void checkGetScrTypeParam(String type) {
        if (isNotEmpty(type) && !SUPPORTED_TYPE.equals(type)) {
            throw new BadRequestException(String.format("Invalid value - %s in field 'type'", type));
        }
    }

    private void checkGetScrSortParam(String sort) {
        if (isNotEmpty(sort) && !SUPPORTED_SORT.equals(sort)) {
            throw new BadRequestException(String.format("Invalid value - %s in field '_sort'", sort));
        }
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
