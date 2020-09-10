package uk.nhs.adaptors.scr.hl7tofhirmappers;

import java.util.Date;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Identifier;

import uk.nhs.adaptors.scr.models.hl7models.BundleObject;

public class BundleMapper {
    public Bundle mapBundle(BundleObject bundleObject) {
        Bundle bundle = new Bundle();

        if (bundleObject.getRepc_IN150016UK05CreationTime() != null){
            bundle.setTimestamp(getTimeStamp(bundleObject));
        }
        if (bundleObject.getRepc_IN150016UK05IDRoot() != null){
            bundle.setIdentifier(getIdentifier(bundleObject));
        }

        //

        return bundle;
    }

    private Identifier getIdentifier(BundleObject bundleObject) {
        Identifier identifier = new Identifier();
        identifier.setValue(bundleObject.getRepc_IN150016UK05IDRoot());
        return identifier;
    }

    private Date getTimeStamp(BundleObject bundleObject) {
        Date date = new Date();
        date.setTime(Long.parseLong(bundleObject.getRepc_IN150016UK05CreationTime()));
        return date;
    }
}
