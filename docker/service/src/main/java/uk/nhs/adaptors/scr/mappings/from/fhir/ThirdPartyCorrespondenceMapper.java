package uk.nhs.adaptors.scr.mappings.from.fhir;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.Communication;
import org.hl7.fhir.r4.model.DateTimeType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.scr.mappings.from.common.UuidWrapper;
import uk.nhs.adaptors.scr.models.xml.ThirdPartyCorrespondence;
import uk.nhs.adaptors.scr.models.xml.ThirdPartyCorrespondenceNote;

import java.util.Date;
import java.util.Map;

import static uk.nhs.adaptors.scr.utils.DateUtil.formatDateToHl7;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
/**
 * Mapping from FHIR to HL7 for Third party correspondence.
 * Despite having the same CMET number, Care professional documentation, and Patient or carer correspondence are
 * mapped separately.
 *
 * CMET: UKCT_MT144035UK01
 */
public class ThirdPartyCorrespondenceMapper {

    public ThirdPartyCorrespondence mapThirdPartyCorrespondence(Communication communication) {
        var obj = new ThirdPartyCorrespondence();
        obj.setIdRoot(communication.getIdentifierFirstRep().getValue());
        var codingFirstRep = communication.getTopic().getCodingFirstRep();

        obj.setCodeCode(codingFirstRep.getCode());
        obj.setCodeDisplayName(codingFirstRep.getDisplay());
        obj.setStatusCodeCode("normal");

        obj.setEffectiveTimeLow(formatDateToHl7(communication.getSentElement()));

        obj.setNote(new ThirdPartyCorrespondenceNote(communication.getNote().get(0).getText()));

        return obj;
    }

    public ThirdPartyCorrespondence mapAdditionalInformationButtonEntry(
            Map<String, String> additionalInformationHeaders) {

        var obj = new ThirdPartyCorrespondence();
        UuidWrapper uuid = new UuidWrapper();

        obj.setIdRoot(uuid.randomUuid());
        obj.setCodeCode("263536004");
        obj.setCodeDisplayName("Communication");
        obj.setStatusCodeCode("normal");
        obj.setEffectiveTimeLow(formatDateToHl7(new DateTimeType(new Date())));
        obj.setNote(new ThirdPartyCorrespondenceNote(
                "Additional information records have been found under the following types:"));

        //Add all the relevant additional information headers which caused the message to appear to the note.
        additionalInformationHeaders.entrySet()
                .forEach(headerEntry -> obj.getNote()
                        .setText(obj.getNote().getText()
                                + "\n" + headerEntry.getKey()));

        return obj;
    }
}
