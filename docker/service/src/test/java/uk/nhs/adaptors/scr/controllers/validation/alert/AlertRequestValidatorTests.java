package uk.nhs.adaptors.scr.controllers.validation.alert;

import org.hl7.fhir.r4.model.AuditEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.nhs.adaptors.scr.components.FhirParser;

import javax.validation.ConstraintValidatorContext;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.nhs.utils.Utils.readResourceFile;

@ExtendWith(MockitoExtension.class)
public class AlertRequestValidatorTests {
    private static final String RESOURCE_DIRECTORY = "alert";
    @InjectMocks
    private AlertRequestValidator alertRequestValidator;
    @Spy
    private FhirParser fhirParser = new FhirParser();

    @Mock
    private ConstraintValidatorContext mockContext;
    @Mock
    private ConstraintValidatorContext.ConstraintViolationBuilder mockBuilder;

    @Test
    public void When_ValidatingHappyPath_Expect_True() {
        // arrange
        var json = readResourceFile(String.format(RESOURCE_DIRECTORY + "/%s.json", "success"));

        // act
        var result = alertRequestValidator.isValid(json, mockContext);

        // assert
        assertThat(result).isTrue();
        verify(fhirParser, times(1)).parseResource(json, AuditEvent.class);
    }

    @Test
    public void When_ValidatingSubTypeOutsideRange_Expect_False() {
        // arrange
        var json = readResourceFile(String.format(RESOURCE_DIRECTORY + "/%s.json", "subtype_range"));
        var outOfBoundsMessage = "Invalid or missing value in field 'subtype.code'. Supported values are: 1, 2, 3, 4, 5, 6";

        when(mockContext.buildConstraintViolationWithTemplate(outOfBoundsMessage)).thenReturn(mockBuilder);

        // act
        var result = alertRequestValidator.isValid(json, mockContext);

        // assert
        assertThat(result).isFalse();
    }

    @Test
    public void When_ValidatingTypeOutsideRange_Expect_False() {
        // arrange
        var json = readResourceFile(String.format(RESOURCE_DIRECTORY + "/%s.json", "type_range"));
        var outOfBoundsMessage = "Invalid or missing value in field 'type.code'. Supported values are: 1, 2";

        when(mockContext.buildConstraintViolationWithTemplate(outOfBoundsMessage)).thenReturn(mockBuilder);

        // act
        var result = alertRequestValidator.isValid(json, mockContext);

        // assert
        assertThat(result).isFalse();
    }
}
