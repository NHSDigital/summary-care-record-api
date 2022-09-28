package uk.nhs.adaptors.scr.mappings.from.hl7;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hl7.fhir.r4.model.PractitionerRole;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import org.hl7.fhir.r4.model.Encounter;
import uk.nhs.adaptors.scr.mappings.from.hl7.common.UuidWrapper;

import java.util.ArrayList;
import java.util.List;

import static uk.nhs.adaptors.scr.utils.FhirHelper.randomUUID;

@Slf4j
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CareEventMapper implements XmlToFhirMapper {

    private final UuidWrapper UUID;

    @Override
    public List<? extends Resource> map(Node document) {
        List<Resource> resources = new ArrayList<>();

        var careEvent = new Encounter();
        careEvent.setId(UUID.RandomUUID());

        resources.add(careEvent);
        return resources;
    }
}
