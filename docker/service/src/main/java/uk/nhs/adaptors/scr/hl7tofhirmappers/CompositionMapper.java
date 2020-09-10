package uk.nhs.adaptors.scr.hl7tofhirmappers;

import static org.hl7.fhir.r4.model.IdType.newRandomUuid;

import java.util.ArrayList;
import java.util.List;

import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.hl7.fhir.r4.model.Composition;
import org.hl7.fhir.r4.model.Identifier;
import org.hl7.fhir.r4.model.Narrative;
import org.hl7.fhir.r4.model.Reference;
import org.hl7.fhir.utilities.xhtml.NodeType;
import org.hl7.fhir.utilities.xhtml.XhtmlNode;

import uk.nhs.adaptors.scr.models.hl7models.CompositionObject;

public class CompositionMapper {
    public Composition mapComposition(CompositionObject compositionObject, Reference patient, List<Reference> practitionerRole){
        Composition composition = new Composition();

        composition.setId(newRandomUuid());
        composition
            .setIdentifier(getIdentifier(compositionObject))
            .setStatus(Composition.CompositionStatus.FINAL) //done
            .setType(getType(compositionObject)) //done
            .setCategory(getCategory(compositionObject)) //done
            .setSubject(patient) //done
            .setDate(compositionObject.getDate()) //done will need to convert date to correct format
            .setAuthor(practitionerRole)
            .setTitle(compositionObject.getTitle())
            .setSection(getSections(compositionObject)); //done

        composition.setRelatesTo(getRelatesTo(compositionObject)); //done

        return composition;
    }

    // need to accept list
    private List<Composition.SectionComponent> getSections(CompositionObject compositionObject) {
        List<Composition.SectionComponent> sectionList = new ArrayList<>();
        Composition.SectionComponent section = new Composition.SectionComponent();

        Narrative narrative = new Narrative();
        narrative.setStatus(Narrative.NarrativeStatus.GENERATED);

        // may need to add:
        /*
        *<div xmlns="http://www.w3.org/1999/xhtml">
        and
        * closing div
        * */
        XhtmlNode xhtmlNode = new XhtmlNode();
        xhtmlNode.setNodeType(NodeType.Document);
        xhtmlNode.addText(compositionObject.getPresentationText());
        narrative.setDiv(xhtmlNode);
        section.setText(narrative);
        section.setTitle(compositionObject.getPresentationTitle());
        section.setCode(
            new CodeableConcept().addCoding(
                new Coding().setCode(
                    compositionObject.getTitleID())));

        sectionList.add(section);

        return sectionList;
    }

    // might need to accept a list or something similar
    private List<CodeableConcept> getCategory(CompositionObject compositionObject) {
        List<CodeableConcept> codeableConceptList = new ArrayList<>();
        CodeableConcept codeableConcept = new CodeableConcept();
        Coding coding = new Coding();
        if (compositionObject.getCategoryCode() != null){
            coding.setCode("163171000000105"); //done
        }
        if (compositionObject.getCategoryCodeSystem() != null){
            coding.setSystem("http://snomed.info/sct");
        }
        if (compositionObject.getCategoryDisplayName() != null){
            coding.setDisplay("Care Professional Documentation");
        }
        if (coding != null){
            codeableConceptList.add(codeableConcept.addCoding(coding));
        }

        return codeableConceptList;
    }

    private CodeableConcept getType(CompositionObject compositionObject) {
        CodeableConcept codeableConcept = new CodeableConcept();
        codeableConcept.addCoding(
            new Coding()
                .setSystem(compositionObject.getGpSummaryCodeSystem()) //done
                .setCode("196981000000101") //done
                .setDisplay(compositionObject.getGpSummaryDisplayName())); //done
        return codeableConcept;
    }

    private Identifier getIdentifier(CompositionObject compositionObject) {
        Identifier identifier = new Identifier();

        identifier
            .setSystem("https://tools.ietf.org/html/rfc4122")
            .setValue(compositionObject.getGpSummaryID()); //done

        return identifier;
    }

    private List<Composition.CompositionRelatesToComponent> getRelatesTo(CompositionObject compositionObject) {
        List<Composition.CompositionRelatesToComponent> componentList = new ArrayList<>();

        if (compositionObject.getReplacementTypeCode() != null){
            Composition.CompositionRelatesToComponent relates = new Composition.CompositionRelatesToComponent();
            relates.setCode(Composition.DocumentRelationshipType.REPLACES);
            componentList.add(relates);
        }
        if (compositionObject.getPriorMessageRef() != null){
            Composition.CompositionRelatesToComponent relates = new Composition.CompositionRelatesToComponent();
            Identifier identifier = new Identifier().setValue(compositionObject.getPriorMessageRef());
            relates.setTarget(identifier);
            componentList.add(relates);
        }

        return componentList;
    }
}
