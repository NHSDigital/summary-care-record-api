package uk.nhs.adaptors.scr.utils.spine.mock.interfaces;

import org.springframework.http.ResponseEntity;

public interface SpineMockSetupWithResponseEntity {
    void withResponseEntity(ResponseEntity responseContent);
}
