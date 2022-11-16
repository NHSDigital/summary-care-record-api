package uk.nhs.adaptors.scr.mappings.from.fhir;

import org.hl7.fhir.r4.model.Condition;
import uk.nhs.adaptors.scr.models.xml.Problem;

public class ProblemMapper {
    public Problem mapProblem(Condition condition) {
        var problem = new Problem();

        problem.setIdRoot(condition.getId());

        return problem;
    }
}
