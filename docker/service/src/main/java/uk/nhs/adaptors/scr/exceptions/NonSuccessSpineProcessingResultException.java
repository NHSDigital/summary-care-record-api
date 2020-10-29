package uk.nhs.adaptors.scr.exceptions;

import java.util.List;
import java.util.stream.Collectors;

public class NonSuccessSpineProcessingResultException extends BadRequestException {
    public NonSuccessSpineProcessingResultException(List<String> errors) {
        super(String.format("Spine processing finished with errors:%n%s",
            errors.stream()
                .map(error -> "- " + error)
                .collect(Collectors.joining("\n"))));
    }
}