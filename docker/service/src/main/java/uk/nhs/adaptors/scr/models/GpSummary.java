package uk.nhs.adaptors.scr.models;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Property;
import org.hl7.fhir.r4.model.Base;
import org.hl7.fhir.r4.model.CodeType;
import org.springframework.stereotype.Component;
import uk.nhs.adaptors.scr.exceptions.FhirMappingException;
import uk.nhs.adaptors.scr.exceptions.FhirValidationException;
import uk.nhs.adaptors.scr.mappings.from.fhir.AuthorMapper;
import uk.nhs.adaptors.scr.mappings.from.fhir.CommunicationMapper;
import uk.nhs.adaptors.scr.mappings.from.fhir.CompositionMapper;
import uk.nhs.adaptors.scr.mappings.from.fhir.ConditionMapper;
import uk.nhs.adaptors.scr.mappings.from.fhir.ObservationMapper;
import uk.nhs.adaptors.scr.mappings.from.fhir.PatientMapper;
import uk.nhs.adaptors.scr.mappings.from.fhir.ProcedureMapper;
import uk.nhs.adaptors.scr.models.xml.CareEvent;
import uk.nhs.adaptors.scr.models.xml.CareProfessionalDocumentation;
import uk.nhs.adaptors.scr.models.xml.Diagnosis;
import uk.nhs.adaptors.scr.models.xml.FamilyHistory;
import uk.nhs.adaptors.scr.models.xml.Finding;
import uk.nhs.adaptors.scr.models.xml.Investigation;
import uk.nhs.adaptors.scr.models.xml.Lifestyle;
import uk.nhs.adaptors.scr.models.xml.Participant;
import uk.nhs.adaptors.scr.models.xml.PatientCarerCorrespondence;
import uk.nhs.adaptors.scr.models.xml.ThirdPartyCorrespondence;
import uk.nhs.adaptors.scr.models.xml.PersonalPreference;
import uk.nhs.adaptors.scr.models.xml.Presentation;
import uk.nhs.adaptors.scr.models.xml.Problem;
import uk.nhs.adaptors.scr.models.xml.ProvisionOfAdviceAndInformation;
import uk.nhs.adaptors.scr.models.xml.RiskToPatient;
import uk.nhs.adaptors.scr.models.xml.SocialOrPersonalCircumstance;
import uk.nhs.adaptors.scr.models.xml.Treatment;
import uk.nhs.adaptors.scr.utils.DateUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.stream.Stream;

import static org.hl7.fhir.r4.model.Bundle.BundleType.DOCUMENT;
import static uk.nhs.adaptors.scr.utils.FhirHelper.UUID_IDENTIFIER_SYSTEM;

@Getter
@Setter
@Slf4j
@Component
public class GpSummary {
    private String headerId;
    private String headerTimeStamp;
    private String compositionId;
    private String compositionDate;
    private String nhsdAsidFrom;
    private String nhsdAsidTo;
    private String partyIdFrom;
    private String partyIdTo;
    private String compositionRelatesToId;
    private String patientId;
    private Participant.Author author;
    private Presentation presentation;
    private List<CareEvent> careEvents = new ArrayList<>();
    private List<CareProfessionalDocumentation> careProfessionalDocumentations = new ArrayList<>();
    private List<Diagnosis> diagnoses = new ArrayList<>();
    private List<FamilyHistory> familyHistories = new ArrayList<>();
    private List<Finding> clinicalObservationsAndFindings = new ArrayList<>();
    private List<Finding> investigationResults = new ArrayList<>();
    private List<Finding> medicationRecommendations = new ArrayList<>();
    private List<Finding> medicationRecords = new ArrayList<>();
    private List<Investigation> investigations = new ArrayList<>();
    private List<Lifestyle> lifestyles = new ArrayList<>();
    private List<PatientCarerCorrespondence> patientCarerCorrespondences = new ArrayList<>();
    private List<PersonalPreference> personalPreferences = new ArrayList<>();
    private List<Problem> problems = new ArrayList<>();
    private List<ProvisionOfAdviceAndInformation> provisionsOfAdviceAndInformationToPatientsAndCarers = new ArrayList<>();
    private List<RiskToPatient> risksToPatient = new ArrayList<>();
    private List<SocialOrPersonalCircumstance> socialOrPersonalCircumstances = new ArrayList<>();
    private List<ThirdPartyCorrespondence> thirdPartyCorrespondences = new ArrayList<>();
    private List<Treatment> treatments = new ArrayList<>();
    private List<String> headers = new ArrayList<>();

    public static GpSummary fromBundle(Bundle bundle, String nhsdAsid) throws FhirMappingException {
        validateType(bundle);

        GpSummary gpSummary = new GpSummary();
        gpSummary.setNhsdAsidFrom(nhsdAsid);

        try {
            Stream.<BiConsumer<GpSummary, Bundle>>of(
                    GpSummary::gpSummarySetHeaderTimeStamp,
                    GpSummary::gpSummarySetHeaderId,
                    AuthorMapper::mapAuthor,
                    CompositionMapper::mapComposition,
                    CommunicationMapper::mapCommunications,
                    ConditionMapper::mapConditions,
                    ObservationMapper::mapObservations,
                    PatientMapper::mapPatient,
                    ProcedureMapper::mapProcedures)
                .forEach(mapper -> mapper.accept(gpSummary, bundle));
        } catch (Exception e) {
            throw new FhirMappingException(e.getMessage(), e.getCause());
        }

        //Check whether additional information is present so that third party correspondence can be injected.
        ImmutablePair<Boolean, Map<String, String>> searchResult = gpSummary.isBundleWithAdditionalInformation(bundle);
        boolean additionalInformation = searchResult.getLeft();

        //Map the additional information only if third party should be injected.
        try {
            if (additionalInformation) {
                CommunicationMapper.mapCommunicationsWithAdditionalInfoButton(gpSummary, bundle);
                //Add additional information message with the additional information types to the first element.
                Map<String, String> additionalInformationHeaders = searchResult.getRight();
                additionalInformationHeaders.entrySet()
                        .forEach(headerEntry -> gpSummary.getThirdPartyCorrespondences().get(0).getNote()
                                .setText(gpSummary.getThirdPartyCorrespondences().get(0).getNote().getText()
                                        + "\n" + headerEntry.getKey()));
            }
        } catch (Exception e) {
            throw new FhirMappingException(e.getMessage(), e.getCause());
        }

        return gpSummary;
    }

    public static ImmutablePair<Boolean, Map<String, String>> isBundleWithAdditionalInformation(Bundle bundle) {
        //A list of all the additional headers for comparison that will trigger third party correspondence.
        Map<String, String> additionalInformationHeaders = new HashMap() {
            {
                put("Administrative Procedures", "ProceduresHeader");
                put("Care Events", "EventsHeader");
                put("Care Professional Documentation", "DocumentationHeader");
                put("Clinical Observations and Findings", "ObservationsHeader");
                put("Diagnoses", "DiagnosesHeader");
                put("Family History", "HistoryHeader");
                put("Investigation Results", "ResultsHeader");
                put("Investigations", "InvestigationsHeader");
                put("Lifestyle", "LifestyleHeader");
                put("Patient/Carer Correspondence", "PatientCarerCorrespondenceHeader");
                put("Personal Preferences", "PreferencesHeader");
                put("Problems and Issues", "ProblemsHeader");
                put("Provision of Advice and Information to Patients and Carers", "AdviceHeader");
                put("Risks to Patient", "RisksToPatientHeader");
                put("Social and Personal Circumstances", "CircumstancesHeader");
                put("Treatments", "TreatmentsHeader");
                put("Risks to Care Professional or Third Party", "RisksToProfessionalHeader");
                put("Services, Care Professionals and Carers", "ServicesHeader");
            }
        };

        Property gpList = bundle.getEntry().get(0).getResource().getChildByName("section"); //entries stored in this section

        //A list that will contain all headers present in the bundle, no matter if they're additional information or not.
        ArrayList<String> headerList = new ArrayList<String>();

        gpList.getValues().forEach(gp -> {
            Property code = gp.getChildByName("code");
            Property coding = code.getValues().get(0).getChildByName("coding");
            Base codingValue = coding.getValues().get(0);

            Base header = codingValue.getChildByName("code").getValues().get(0); //header extracted in this variable

            headerList.add(((CodeType) header).getCode());
        });

        //assign all the headers that are additional information and present in the bundle. Removes duplicates, if any.
        Map<String, String> additionalInformationHeadersPresent = new HashMap();
        additionalInformationHeaders.entrySet().stream()
                .filter(headerEntry -> headerList.contains(headerEntry.getValue()))
                .forEach(entry -> additionalInformationHeadersPresent.put(entry.getKey(), entry.getValue()));

        if (additionalInformationHeadersPresent.size() > 0) {
            return new ImmutablePair<>(true, additionalInformationHeadersPresent);
        }

        System.out.println("No additional information headers found!");

        return new ImmutablePair<>(false, additionalInformationHeadersPresent);
    }

    private static void validateType(Bundle bundle) {
        if (!DOCUMENT.equals(bundle.getType())) {
            throw new FhirValidationException("Unsupported Bundle.type: " + bundle.getType());
        }
    }

    private static void gpSummarySetHeaderId(GpSummary gpSummary, Bundle bundle) {
        if (bundle.hasIdentifier()) {
            var identifier = bundle.getIdentifier();
            if (!UUID_IDENTIFIER_SYSTEM.equals(identifier.getSystem())) {
                throw new FhirMappingException(String.format("bundle.identifier.system must be %s", UUID_IDENTIFIER_SYSTEM));
            }
            if (bundle.getIdentifier().hasValue()) {
                gpSummary.setHeaderId(bundle.getIdentifier().getValue().toUpperCase());
            } else {
                throw new FhirMappingException("bundle.identifier.value must not be empty");
            }
        } else {
            throw new FhirMappingException("bundle.identifier must not be empty");
        }
    }

    private static void gpSummarySetHeaderTimeStamp(GpSummary gpSummary, Bundle bundle) {
        if (bundle.hasTimestampElement()) {
            gpSummary.setHeaderTimeStamp(DateUtil.formatTimestampToHl7(bundle.getTimestampElement()));
        } else {
            throw new FhirMappingException("bundle.timestamp must not be empty");
        }
    }
}
