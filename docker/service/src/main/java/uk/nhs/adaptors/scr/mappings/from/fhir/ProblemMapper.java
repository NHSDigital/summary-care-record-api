package uk.nhs.adaptors.scr.mappings.from.fhir;

import org.hl7.fhir.r4.model.Condition;
import uk.nhs.adaptors.scr.models.xml.Diagnosis;
import uk.nhs.adaptors.scr.models.xml.Problem;

import static uk.nhs.adaptors.scr.utils.DateUtil.formatDateToHl7;

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

        if (condition.hasStage()) {
            mapStages(condition, problem);
        }

        return problem;
    }

    private void mapStages(Condition condition, Problem problem) {
        var stage = condition.getStage();
        var diagnosisId = "";
        problem.setDiagnosisId(diagnosisId);
    }
}
