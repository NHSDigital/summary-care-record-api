package uk.nhs.adaptors.scr.controllers.validation.acs;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Parameters;
import org.hl7.fhir.r4.model.Parameters.ParametersParameterComponent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.scr.components.FhirParser;
import uk.nhs.adaptors.scr.exceptions.FhirValidationException;
import uk.nhs.adaptors.scr.models.AcsPermission;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import static org.springframework.util.StringUtils.isEmpty;

@Component
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AcsRequestValidator implements ConstraintValidator<AcsRequest, String> {

    private static final String SET_PERMISSION_PARAM_NAME = "setPermissions";
    private static final String NHS_NUMBER_PART_NAME = "nhsNumber";
    private static final String PERMISSION_CODE_PART_NAME = "permissionCode";
    private static final String PERMISSION_CODE_SYSTEM = "https://fhir.nhs.uk/CodeSystem/SCR-ACSPermission";

    private final FhirParser fhirParser;

    @Override
    public void initialize(AcsRequest contactNumber) {
    }

    @Override
    public boolean isValid(String requestBody, ConstraintValidatorContext context) {
        try {
            Parameters parameters = fhirParser.parseResource(requestBody, Parameters.class);

            ParametersParameterComponent parameter = getSetPermissionParameter(parameters);
            checkNhsNumber(parameter);
            checkPermission(parameter);
        } catch (FhirValidationException exc) {
            setErrorMessage(context, exc.getMessage());
            return false;
        }

        return true;
    }

    private static void checkNhsNumber(Parameters.ParametersParameterComponent parameter) {
        parameter.getPart().stream()
            .filter(p -> NHS_NUMBER_PART_NAME.equals(p.getName()))
            .filter(p -> !isEmpty(p.getValue()))
            .reduce((x, y) -> {
                throw new FhirValidationException(String.format("Exactly 1 Parameter.Part named '%s' with not empty value expected",
                    NHS_NUMBER_PART_NAME));
            })
            .orElseThrow(() -> new FhirValidationException(String.format(
                "Parameter.Part named '%s' with not empty value not found", NHS_NUMBER_PART_NAME)));
    }

    private static void checkPermission(Parameters.ParametersParameterComponent parameter) {
        Coding coding = (Coding) parameter.getPart().stream()
            .filter(p -> PERMISSION_CODE_PART_NAME.equals(p.getName()))
            .filter(p -> PERMISSION_CODE_SYSTEM.equals(((Coding) p.getValue()).getSystem()))
            .reduce((x, y) -> {
                throw new FhirValidationException(String.format("Exactly 1 Parameter.Part named '%s' with valueCoding.system %s expected",
                    PERMISSION_CODE_PART_NAME, PERMISSION_CODE_SYSTEM));
            })
            .orElseThrow(() -> new FhirValidationException(String.format(
                "Parameter.Part named '%s' with valueCoding.system %s not found", PERMISSION_CODE_PART_NAME, PERMISSION_CODE_SYSTEM))
            )
            .getValue();

        String permissionValue = coding.getCode();
        try {
            AcsPermission.fromValue(permissionValue);
        } catch (Exception e) {
            LOGGER.error("Invalid permission value: " + permissionValue, e);
            throw new FhirValidationException(String.format("Invalid value - %s in field 'valueCoding.code'", permissionValue));
        }
    }

    private static ParametersParameterComponent getSetPermissionParameter(Parameters parameters) {
        return parameters.getParameter().stream()
            .filter(p -> SET_PERMISSION_PARAM_NAME.equals(p.getName()))
            .reduce((x, y) -> {
                throw new FhirValidationException(String.format("Exactly 1 parameter named '%s' expected", SET_PERMISSION_PARAM_NAME));
            })
            .orElseThrow(() -> new FhirValidationException(String.format(
                "Parameter named '%s' name not found", SET_PERMISSION_PARAM_NAME))
            );
    }

    private void setErrorMessage(ConstraintValidatorContext cxt, String message) {
        cxt.disableDefaultConstraintViolation();
        cxt.buildConstraintViolationWithTemplate(message)
            .addConstraintViolation();
    }
}
