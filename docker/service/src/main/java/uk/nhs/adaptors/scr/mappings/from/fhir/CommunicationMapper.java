package uk.nhs.adaptors.scr.mappings.from.fhir;

import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Communication;
import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.models.xml.PatientCarerCorrespondence;
import uk.nhs.adaptors.scr.models.xml.ProvisionOfAdviceAndInformation;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static uk.nhs.adaptors.scr.utils.DateUtil.formatDateToHl7;
import static uk.nhs.adaptors.scr.utils.FhirHelper.getDomainResourceList;

public class CommunicationMapper {
    private static final Predicate<Communication> IS_PROVISIONS_OF_ADVICE_AND_INFORMATION_TO_PATIENTS_AND_CARERS =
        communication -> "163101000000102".equals(communication.getCategoryFirstRep().getCodingFirstRep().getCode());
    private static final Predicate<Communication> IS_PATIENT_CARER_CORRESPONDENCES =
        communication -> "163181000000107".equals(communication.getCategoryFirstRep().getCodingFirstRep().getCode());

    public static void mapCommunications(GpSummary gpSummary, Bundle bundle) {
        gpSummary.getProvisionsOfAdviceAndInformationToPatientsAndCarers()
            .addAll(mapProvisionOfAdviceAndInformationToPatientsAndCarers(bundle));
        gpSummary.getPatientCarerCorrespondences()
            .addAll(mapPatientCarerCorrespondences(bundle));
    }

    private static List<ProvisionOfAdviceAndInformation> mapProvisionOfAdviceAndInformationToPatientsAndCarers(Bundle bundle) {
        return getDomainResourceList(bundle, Communication.class).stream()
            .filter(IS_PROVISIONS_OF_ADVICE_AND_INFORMATION_TO_PATIENTS_AND_CARERS)
            .map(CommunicationMapper::mapProvisionOfAdviceAndInformationToPatientsAndCarers)
            .collect(Collectors.toList());
    }

    private static List<PatientCarerCorrespondence> mapPatientCarerCorrespondences(Bundle bundle) {
        return getDomainResourceList(bundle, Communication.class).stream()
            .filter(IS_PATIENT_CARER_CORRESPONDENCES)
            .map(CommunicationMapper::mapPatientCarerCorrespondence)
            .collect(Collectors.toList());
    }

    private static ProvisionOfAdviceAndInformation mapProvisionOfAdviceAndInformationToPatientsAndCarers(Communication communication) {
        return new ProvisionOfAdviceAndInformation()
            .setIdRoot(communication.getIdentifierFirstRep().getValue())
            .setStatusCodeCode(mapStatus(communication.getStatus()))
            .setCodeCode(communication.getTopic().getCodingFirstRep().getCode())
            .setCodeDisplayName(communication.getTopic().getCodingFirstRep().getDisplay())
            .setEffectiveTimeLow(formatDateToHl7(communication.getSent()));
    }

    private static PatientCarerCorrespondence mapPatientCarerCorrespondence(Communication communication) {
        return new PatientCarerCorrespondence()
            .setIdRoot(communication.getIdentifierFirstRep().getValue())
            .setStatusCodeCode(mapStatus(communication.getStatus()))
            .setCodeCode(communication.getTopic().getCodingFirstRep().getCode())
            .setCodeDisplayName(communication.getTopic().getCodingFirstRep().getDisplay())
            .setEffectiveTimeLow(formatDateToHl7(communication.getSent()));
    }

    private static String mapStatus(Communication.CommunicationStatus status) {
        switch (status) {
            case PREPARATION:
            case INPROGRESS:
            case NOTDONE:
            case ONHOLD:
            case STOPPED:
                return "active";
            case COMPLETED:
                return "completed";
            case ENTEREDINERROR:
            case UNKNOWN:
            case NULL:
            default:
                return "nullified";
        }
    }
}
