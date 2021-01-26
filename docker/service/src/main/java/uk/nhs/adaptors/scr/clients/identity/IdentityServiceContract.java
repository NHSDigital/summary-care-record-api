package uk.nhs.adaptors.scr.clients.identity;

public interface IdentityServiceContract {
    UserInfo getUserInfo(String authorization);
}
