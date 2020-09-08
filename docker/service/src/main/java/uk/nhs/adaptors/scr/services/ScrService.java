package uk.nhs.adaptors.scr.services;

import com.github.mustachejava.Mustache;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Bundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.scr.clients.SpineClient;
import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.utils.GpSummaryParser;
import uk.nhs.adaptors.scr.utils.TemplateUtils;

@Component
@Slf4j
public class ScrService {
    @Autowired
    private SpineClient spineClient;

    private static final Mustache REPC_RM150007UK05_TEMPLATE =
        TemplateUtils.loadTemplate("REPC_RM150007UK05.mustache");

    public void handleFhir(Bundle resource) {
        GpSummary gpSummary = GpSummaryParser.parseFromBundle(resource);
        String spineRequest = TemplateUtils.fillTemplate(REPC_RM150007UK05_TEMPLATE, gpSummary);

        var response = spineClient.sendScrData(spineRequest);
        var requestIdentifier = getRequestIdentifier(response);
        spineClient.getScrProcessingResult(requestIdentifier);
        //TODO: map response to FHIR and return back to controller
    }

    private String getRequestIdentifier(String response) {
        //TODO: extract request identifier from response
        return "123";
    }
}
