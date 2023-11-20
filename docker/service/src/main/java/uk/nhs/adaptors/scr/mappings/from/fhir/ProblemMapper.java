package uk.nhs.adaptors.scr.mappings.from.fhir;

import org.hl7.fhir.r4.model.Condition;
import uk.nhs.adaptors.scr.models.xml.Problem;

import static uk.nhs.adaptors.scr.utils.DateUtil.formatDateToHl7;

/**
 * Mapping from FHIR to HL7 ProblemMapper for highlighting a clinical statement as a problem
 *
 * CMET: UKCT_MT144038UK02
 */

public class ProblemMapper {
    public Problem mapProblem(Condition condition) {
        var problem = new Problem();

        problem.setIdRoot(condition.getIdentifierFirstRep().getValue());

        var codingFirstRep = condition.getCode().getCodingFirstRep();
        problem.setCodeCode(codingFirstRep.getCode());
        problem.setCodeDisplayName(codingFirstRep.getDisplay());

        problem.setStatusCodeCode("active");

        problem.setEffectiveTimeLow(condition.getId());

        problem.setEffectiveTimeLow(formatDateToHl7(condition.getOnsetDateTimeType()));

        // Commented out, awaiting further information and action in NIAD-2505
        // set diagnosis reference?

        return problem;
    }
}
