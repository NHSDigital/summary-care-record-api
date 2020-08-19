package uk.nhs.adaptors.scr.utils;

import org.hl7.fhir.r4.model.Bundle;

import uk.nhs.adaptors.scr.models.GpSummary;

public class GpSummaryParser {
    public static GpSummary parseFromBundle(Bundle bundle) {
        GpSummary gpSummary = new GpSummary();

        // TODO: Update fields in mapping stories
        gpSummary.setSomeField("1");

        return gpSummary;
    }
}
