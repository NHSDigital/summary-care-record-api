package uk.nhs.adaptors.scr.mappings.from.fhir;

import com.github.mustachejava.Mustache;
import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.scr.config.ScrConfiguration;
import uk.nhs.adaptors.scr.exceptions.FhirMappingException;
import uk.nhs.adaptors.scr.logging.LogExecutionTime;
import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.utils.TemplateUtils;

/**
 * This is the origin mapper which subsequently calls all the other mappers (through GpSummary.fomBundle).
 * It fills in a template file which contains the encompassing SOAP envelope for the HL7 document.
 * FHIR to HL7 conversion involves extracting values from parsable FHIR (JSON) and using it to fill in
 * mustache templates.
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class BundleMapper {
    private static final Mustache REPC_RM150007UK05_TEMPLATE =
        TemplateUtils.loadTemplate("REPC_RM150007UK05.mustache");

    private final ScrConfiguration scrConfiguration;

    @LogExecutionTime
    public String map(Bundle bundle, String nhsdAsid) {
        try {
            // Call mappers for each resource type.
            GpSummary gpSummary = GpSummary.fromBundle(bundle, nhsdAsid);
            gpSummary.setPartyIdFrom(scrConfiguration.getPartyIdFrom());
            gpSummary.setPartyIdTo(scrConfiguration.getPartyIdTo());
            gpSummary.setNhsdAsidTo(scrConfiguration.getNhsdAsidTo());
            return TemplateUtils.fillTemplate(REPC_RM150007UK05_TEMPLATE, gpSummary);

        } catch (Exception ex) {
            throw new FhirMappingException(ex.getMessage());
        }
    }
}
