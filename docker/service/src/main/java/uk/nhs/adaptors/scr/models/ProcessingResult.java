package uk.nhs.adaptors.scr.models;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import uk.nhs.adaptors.scr.exceptions.ParseProcessingResultException;

import java.util.regex.Pattern;

@Data
@Slf4j
public class ProcessingResult {
    private static final Pattern SOAP_ENVELOPE_PATTERN = Pattern.compile("(<soap:Envelope.+<\\/soap:Envelope>)");
    private static final Pattern HL7_PATTERN = Pattern.compile("(<hl7:MCCI_IN010000UK13.+<\\/hl7:MCCI_IN010000UK13>)");

    private String soapEnvelope;
    private String hl7;

    public static ProcessingResult parseProcessingResult(String response) {
        var soapEnvelope = SOAP_ENVELOPE_PATTERN
            .matcher(response)
            .results()
            .findFirst()
            .orElseThrow(() -> {
                LOGGER.error("Unable to extract SOAP Envelope from processing result response:\n{}", response);
                return new ParseProcessingResultException("Unable to extract SOAP Envelope from processing result response");
            })
            .group();
        var hl7 = HL7_PATTERN
            .matcher(response)
            .results()
            .findFirst()
            .orElseThrow(() -> {
                LOGGER.error("Unable to extract HL7 from processing result response:\n{}", response);
                return new ParseProcessingResultException("Unable to extract HL7 from processing result response");
            })
            .group();

        return new ProcessingResult()
            .setSoapEnvelope(soapEnvelope)
            .setHl7(hl7);
    }
}
