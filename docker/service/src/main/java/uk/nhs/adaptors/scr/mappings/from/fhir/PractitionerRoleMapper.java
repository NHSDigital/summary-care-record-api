package uk.nhs.adaptors.scr.mappings.from.fhir;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.PractitionerRole;
import uk.nhs.adaptors.scr.exceptions.FhirMappingException;
import uk.nhs.adaptors.scr.models.GpSummary;
import uk.nhs.adaptors.scr.models.gpsummarymodels.PractitionerRoleCode;
import uk.nhs.adaptors.scr.models.gpsummarymodels.PractitionerRoleCodeCoding;
import uk.nhs.adaptors.scr.models.gpsummarymodels.PractitionerRoleIdentifier;

import java.util.ArrayList;
import java.util.List;

public class PractitionerRoleMapper {

    private static final String AGENT_PERSON_SDS_LINK = "http://fhir.nhs.net/Id/sds-role-profile-id";
    private static final String AGENT_PERSON_LINK = "https://fhir.nhs.uk/R4/CodeSystem/UKCore-SDSJobRoleName";

    public static void mapPractitionerRole(GpSummary gpSummary, PractitionerRole practitionerRole) throws FhirMappingException {
        setPractitionerRoleIdentifiers(gpSummary, practitionerRole);
        setPractitionerRoleCodes(gpSummary, practitionerRole);
    }

    private static void setPractitionerRoleIdentifiers(GpSummary gpSummary, PractitionerRole practitionerRole) throws FhirMappingException {
        List<PractitionerRoleIdentifier> practitionerRoleIdentifiers = new ArrayList<>();

        if (practitionerRole.hasIdentifier()) {
            for (Identifier identifier : practitionerRole.getIdentifier()) {
                PractitionerRoleIdentifier practitionerRoleIdentifier = new PractitionerRoleIdentifier();

                if (identifier.hasValue()) {
                    practitionerRoleIdentifier.setPractitionerRoleId(identifier.getValue());
                }

                if (identifier.hasSystem()) {
                    if (identifier.getSystem().equalsIgnoreCase(AGENT_PERSON_SDS_LINK)) {
                        practitionerRoleIdentifier.setPractitionerRoleIsAgentPersonSDS(true);
                    } else if (identifier.getSystem().equalsIgnoreCase(AGENT_PERSON_LINK)) {
                        practitionerRoleIdentifier.setPractitionerRoleIsAgentPersonSDS(false);
                    } else {
                        throw new FhirMappingException("PractitionerRole identifier system value incorrect. It must be equal to \""
                            + AGENT_PERSON_SDS_LINK + "\" or \"" + AGENT_PERSON_LINK + "\".");
                    }
                } else {
                    throw new FhirMappingException("PractitionerRole identifier system missing from payload");
                }

                practitionerRoleIdentifiers.add(practitionerRoleIdentifier);
            }
        } else {
            throw new FhirMappingException("PractitionerRole identifier missing from payload");
        }

        gpSummary.setPractitionerRoleIdentifiers(practitionerRoleIdentifiers);
    }

    private static void setPractitionerRoleCodes(GpSummary gpSummary, PractitionerRole practitionerRole)
        throws FhirMappingException {
        List<PractitionerRoleCode> practitionerRoleCodes = new ArrayList<>();

        if (practitionerRole.hasCode()) {
            for (CodeableConcept codeableConcept : practitionerRole.getCode()) {
                PractitionerRoleCode practitionerRoleCode = new PractitionerRoleCode();
                if (codeableConcept.hasCoding()) {
                    List<PractitionerRoleCodeCoding> practitionerRoleCodeCodings = new ArrayList<>();
                    for (Coding coding : codeableConcept.getCoding()) {
                        if (coding.hasCode() || coding.hasDisplay()) {
                            PractitionerRoleCodeCoding practitionerRoleCodeCoding = new PractitionerRoleCodeCoding();
                            if (coding.hasCode()) {
                                practitionerRoleCodeCoding.setPractitionerRoleCode(coding.getCode());
                            }

                            if (coding.hasDisplay()) {
                                practitionerRoleCodeCoding.setPractitionerRoleCodeDisplayName(coding.getCode());
                            }
                            practitionerRoleCodeCodings.add(practitionerRoleCodeCoding);
                        }
                    }
                    practitionerRoleCode.setPractitionerRoleCodeCodings(practitionerRoleCodeCodings);
                }
                practitionerRoleCodes.add(practitionerRoleCode);
            }
        }

        gpSummary.setPractitionerRoleCodes(practitionerRoleCodes);
    }
}
