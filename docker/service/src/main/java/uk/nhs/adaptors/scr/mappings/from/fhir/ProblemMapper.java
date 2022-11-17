package uk.nhs.adaptors.scr.mappings.from.fhir;

import org.hl7.fhir.r4.model.Condition;
import uk.nhs.adaptors.scr.models.xml.Diagnosis;
import uk.nhs.adaptors.scr.models.xml.Problem;

import static uk.nhs.adaptors.scr.utils.DateUtil.formatDateToHl7;

public class ProblemMapper {
    public Problem mapProblem(Condition condition) {
        var problem = new Problem();

        problem.setIdRoot(condition.getId());

        var codingFirstRep = condition.getCode().getCodingFirstRep();
        problem.setCodeCode(codingFirstRep.getCode());
        problem.setCodeDisplayName(codingFirstRep.getDisplay());

        problem.setStatusCodeCode("active");

        problem.setEffectiveTimeLow(condition.getId());

        problem.setEffectiveTimeLow(formatDateToHl7(condition.getOnsetDateTimeType()));

        problem.setDiagnosisId("D680F6BE-73B9-4E18-988B-1D55E1B6F2D5");

        return problem;
    }
}
