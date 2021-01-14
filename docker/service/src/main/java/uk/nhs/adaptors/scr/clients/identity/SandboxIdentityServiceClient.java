package uk.nhs.adaptors.scr.clients.identity;

public class SandboxIdentityServiceClient implements IdentityServiceContract {
    @Override
    public UserInfo getUserInfo(String baseHost, String authorization) {
        return null;
    }
}
