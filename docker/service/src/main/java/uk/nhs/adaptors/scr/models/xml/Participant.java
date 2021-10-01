package uk.nhs.adaptors.scr.models.xml;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class Participant {

    private String time;
    private AgentPerson agentPerson;
    private AgentPersonSDS agentPersonSDS;

    @Getter
    @Setter
    public static class Author extends Participant {
    }

    @Getter
    @Setter
    public static class Author1 extends Author {
        private AgentDevice agentDevice;
    }

    @Getter
    @Setter
    public static class Informant extends Participant {
        private NonAgentRole participantNonAgentRole;
    }

    @Getter
    @Setter
    public static class Performer extends Participant {
        private String modeCodeCode;
    }
}
