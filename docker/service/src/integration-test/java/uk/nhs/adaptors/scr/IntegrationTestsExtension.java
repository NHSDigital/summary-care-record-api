package uk.nhs.adaptors.scr;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.nhs.adaptors.scr.container.MockSpineContainer;
import uk.nhs.adaptors.scr.utils.spine.mock.SpineMockSetupEndpoint;

@Slf4j
public class IntegrationTestsExtension implements BeforeAllCallback, BeforeEachCallback {

    @Override
    public void beforeAll(ExtensionContext context) {
        MockSpineContainer.getInstance().start();
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        var applicationContext = SpringExtension.getApplicationContext(context);

        var spineMockUrl = applicationContext.getEnvironment().getProperty("spine.url");
        var spineMockSetupEndpoint = applicationContext.getBean(SpineMockSetupEndpoint.class);
        spineMockSetupEndpoint.reset(spineMockUrl);
    }
}
