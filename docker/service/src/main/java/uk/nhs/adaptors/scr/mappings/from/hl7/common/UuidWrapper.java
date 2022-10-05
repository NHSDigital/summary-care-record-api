package uk.nhs.adaptors.scr.mappings.from.hl7.common;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.UUID;

@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UuidWrapper implements IUuidWrapper {

    @Override
    public String randomUuid() {
        return UUID.randomUUID().toString().toUpperCase();
    }
}
