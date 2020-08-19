package uk.nhs.adaptors.scr.services;

import org.hl7.fhir.r4.model.Bundle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.mustachejava.Mustache;

import uk.nhs.adaptors.scr.clients.SpineClient;
import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.utils.GpSummaryParser;
import uk.nhs.adaptors.scr.utils.TemplateUtils;

@Component
public class ScrService {
    @Autowired
    private SpineClient spineClient;

    private static final Mustache REPC_RM150007UK05_TEMPLATE = TemplateUtils.loadTemplate("REPC_RM150007UK05.mustache");

    public void handleFhir(Bundle resource) {
        GpSummary gpSummary = GpSummaryParser.parseFromBundle(resource);
        String spineRequest = TemplateUtils.fillTemplate(REPC_RM150007UK05_TEMPLATE, gpSummary);

        spineClient.sendSpineRequest(spineRequest);
    }
}
