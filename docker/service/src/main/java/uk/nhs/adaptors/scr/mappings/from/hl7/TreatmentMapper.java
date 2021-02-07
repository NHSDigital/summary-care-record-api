package uk.nhs.adaptors.scr.mappings.from.hl7;

import lombok.RequiredArgsConstructor;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import uk.nhs.adaptors.scr.mappings.from.hl7.common.ProcedureMapper;

import java.util.List;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TreatmentMapper implements XmlToFhirMapper {

    private final ProcedureMapper procedureMapper;

    @Override
    public List<? extends Resource> map(Node document) {
        return procedureMapper.map(document, "UKCT_MT144055UK01.Treatment");
    }
}
