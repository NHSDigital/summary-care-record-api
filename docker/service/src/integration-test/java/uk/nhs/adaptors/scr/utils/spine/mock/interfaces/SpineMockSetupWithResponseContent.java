package uk.nhs.adaptors.scr.utils.spine.mock.interfaces;

import org.springframework.http.ResponseEntity;

public interface SpineMockSetupWithResponseContent {
    void withResponseContent(String responseContent);
    void withResponseEntity(ResponseEntity responseEntity);
}
