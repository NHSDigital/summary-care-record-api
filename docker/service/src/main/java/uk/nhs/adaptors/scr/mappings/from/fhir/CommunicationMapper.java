package uk.nhs.adaptors.scr.mappings.from.fhir;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Communication;
import uk.nhs.adaptors.scr.exceptions.FhirValidationException;
import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.models.xml.PatientCarerCorrespondence;
import uk.nhs.adaptors.scr.models.xml.ProvisionOfAdviceAndInformation;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static uk.nhs.adaptors.scr.utils.FhirHelper.getDomainResourceList;

public class CommunicationMapper {
    private static final Predicate<Communication> IS_PATIENT_CARER_CORRESPONDENCE =
        communication -> "163181000000107".equals(communication.getCategory().getCodingFirstRep().getCode());
    private static final Predicate<Communication> IS_PROVISION_OF_ADVICE_AND_INFORMATION =
        communication -> "163101000000102".equals(communication.getCategory().getCodingFirstRep().getCode());

    public static void mapCommunications(GpSummary gpSummary, Bundle bundle) {
        validate(bundle);
        gpSummary.getPatientCarerCorrespondences()
            .addAll(mapPatientAndCarersCorrespondence(bundle));
        gpSummary.getProvisionsOfAdviceAndInformationToPatientsAndCarers()
            .addAll(mapProvisionOfAdviceAndInformation(bundle));
    }


    /**
     * Mapping of Patient/carer correspondence
     * @param bundle
     * @return PatientCarerCorrespondence List
     */
    private static List<PatientCarerCorrespondence> mapPatientAndCarersCorrespondence(Bundle bundle) {
        var patientAndCarersCorrespondenceMapper = new PatientAndCarersCorrespondenceMapper();
        return getDomainResourceList(bundle, Communication.class).stream()
            .filter(IS_PATIENT_CARER_CORRESPONDENCE)
            .map(communication -> patientAndCarersCorrespondenceMapper.mapPatientCarerCorrespondence(communication))
            .collect(Collectors.toList());
    }

    /**
     * Mapping of Provision of Advice and Information to Patients and Carers
     * @param bundle
     * @return ProvisionOfAdviceAndInformation List
     */
    private static List<ProvisionOfAdviceAndInformation> mapProvisionOfAdviceAndInformation(Bundle bundle) {
        var provisionOfAdviceAndInformationMapper = new ProvisionOfAdviceAndInfoMapper();
        return getDomainResourceList(bundle, Communication.class).stream()
            .filter(IS_PROVISION_OF_ADVICE_AND_INFORMATION)
            .map(communication -> provisionOfAdviceAndInformationMapper.mapProvisionOfAdviceInfo(communication))
            .collect(Collectors.toList());
    }

    private static void validate(Bundle bundle) {
        getDomainResourceList(bundle, Communication.class).stream()
            .forEach(it -> {
                if (!it.hasId()) {
                    throw new FhirValidationException("Procedure.id is missing");
                }
                if (!it.hasCode()) {
                    throw new FhirValidationException("Procedure.code is missing");
                }
            });
    }
}
