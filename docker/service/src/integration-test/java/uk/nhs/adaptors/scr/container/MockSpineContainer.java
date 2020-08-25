package uk.nhs.adaptors.scr.container;

import org.testcontainers.containers.GenericContainer;

public final class MockSpineContainer extends GenericContainer<MockSpineContainer> {

    public static final int MOCK_PORT = 8081;
    private static MockSpineContainer container;

    private MockSpineContainer() {
        super("uk.nhs/spine-mock:0.0.1-SNAPSHOT");
        addExposedPort(MOCK_PORT);
    }

    public static MockSpineContainer getInstance() {
        if (container == null) {
            container = new MockSpineContainer();
        }
        return container;
    }

    @Override
    public void start() {
        super.start();
        var containerUri = "http://localhost:" + getMappedPort(MOCK_PORT);
        System.setProperty("SCR_SPINE_URL", containerUri);
    }
}
