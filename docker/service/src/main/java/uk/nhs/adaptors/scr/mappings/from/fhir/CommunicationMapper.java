package uk.nhs.adaptors.scr.mappings.from.fhir;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Communication;
import uk.nhs.adaptors.scr.exceptions.FhirValidationException;
import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.models.xml.CareProfessionalDocumentation;
import uk.nhs.adaptors.scr.models.xml.PatientCarerCorrespondence;
import uk.nhs.adaptors.scr.models.xml.ProvisionOfAdviceAndInformation;
import uk.nhs.adaptors.scr.models.xml.ThirdPartyCorrespondence;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static uk.nhs.adaptors.scr.utils.FhirHelper.getDomainResourceList;

public class CommunicationMapper {
    /**
     * Documentation of SNOMED code, see: http://bit.ly/3YL32JY
     */
    private static final Predicate<Communication> IS_PATIENT_CARER_CORRESPONDENCE =
        communication -> "163181000000107".equals(communication.getCategoryFirstRep().getCodingFirstRep().getCode());
    /**
     * Documentation of SNOMED code, see: http://bit.ly/3yASZws
     */
    private static final Predicate<Communication> IS_PROVISION_OF_ADVICE_AND_INFORMATION =
        communication -> "163101000000102".equals(communication.getCategoryFirstRep().getCodingFirstRep().getCode());
    /**
     * Documentation of SNOMED code, see: http://bit.ly/3JDtzoh
     */
    private static final Predicate<Communication> IS_CARE_PROFESSIONAL_DOCUMENTATION =
        communication -> "163171000000105".equals(communication.getCategoryFirstRep().getCodingFirstRep().getCode());
    /**
     * Documentation of SNOMED code, see: http://bit.ly/3ZY0qJA
     */
    private static final Predicate<Communication> IS_THIRD_PARTY_CORRESPONDENCE =
        communication -> "163191000000109".equals(communication.getCategoryFirstRep().getCodingFirstRep().getCode());

    public static void mapCommunications(GpSummary gpSummary, Bundle bundle) {
        validate(bundle);
        gpSummary.getPatientCarerCorrespondences()
            .addAll(mapPatientAndCarersCorrespondence(bundle));
        gpSummary.getProvisionsOfAdviceAndInformationToPatientsAndCarers()
            .addAll(mapProvisionOfAdviceAndInformation(bundle));
    }

    public static void mapCommunicationsWithAdditionalInfoButton(GpSummary gpSummary, Bundle bundle,
                                                                 Map<String, String> additionalInformationHeaders) {
        validate(bundle);
        gpSummary.getThirdPartyCorrespondences()
            .add(mapAdditionalInformationButton(bundle, additionalInformationHeaders));
        /*TODO: When the actual mapping gets done, add the correspondences as well.
           But the one with the additional information message will always be first.
        gpSummary.getThirdPartyCorrespondences()
            .addAll(mapThirdPartyCorrespondence(bundle));*/

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

    /**
     * Mapping of Third Party Correspondence
     * @param bundle
     * @return ProvisionOfAdviceAndInformation List
     */
    private static List<ThirdPartyCorrespondence> mapThirdPartyCorrespondence(Bundle bundle) {
        var thirdPartyCorrespondenceMapper = new ThirdPartyCorrespondenceMapper();

        return getDomainResourceList(bundle, Communication.class).stream()
            .filter(IS_THIRD_PARTY_CORRESPONDENCE)
            .map(communication -> thirdPartyCorrespondenceMapper.mapThirdPartyCorrespondence(communication))
            .collect(Collectors.toList());
    }

    /**
     * Mapping of Third Party Correspondence
     * @param bundle
     * @return ProvisionOfAdviceAndInformation List
     */
    private static ThirdPartyCorrespondence mapAdditionalInformationButton(
            Bundle bundle,
            Map<String, String> additionalInformationHeaders) {
        var thirdPartyCorrespondenceMapper = new ThirdPartyCorrespondenceMapper();

        List<ThirdPartyCorrespondence> thirdPartyCorrespondences = new ArrayList<>();

        thirdPartyCorrespondences.add(thirdPartyCorrespondenceMapper
                .mapAdditionalInformationButtonEntry(additionalInformationHeaders));

        //If no valid information is present in the bundle, create an empty entry.
        if (thirdPartyCorrespondences.isEmpty()) {
            return new ThirdPartyCorrespondence();
        }

        return thirdPartyCorrespondences.get(0);
    }

    /**
     * Mapping of Care Professional Documentation
     * @param bundle
     * @return CareProfessionalDocumentation List
     */
    private static List<CareProfessionalDocumentation> mapCareProfessionalDocumentation(Bundle bundle) {
        var careProfessionalDocumentationMapper = new CareProfessionalDocumentationMapper();
        return getDomainResourceList(bundle, Communication.class).stream()
            .filter(IS_CARE_PROFESSIONAL_DOCUMENTATION)
            .map(communication -> careProfessionalDocumentationMapper.map(communication))
            .collect(Collectors.toList());
    }

    private static void validate(Bundle bundle) {
        getDomainResourceList(bundle, Communication.class).stream()
            .forEach(it -> {
                if (!it.hasId()) {
                    throw new FhirValidationException("Communication.id is missing");
                }
                if (!it.hasTopic()) {
                    throw new FhirValidationException("Communication.code is missing");
                }
            });
    }
}
