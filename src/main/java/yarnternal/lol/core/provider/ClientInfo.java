package yarnternal.lol.core.provider;

public interface ClientInfo {
    String clientName();

    String clientVersion();

    String clientBranch();

    String getFullInfo();
}
