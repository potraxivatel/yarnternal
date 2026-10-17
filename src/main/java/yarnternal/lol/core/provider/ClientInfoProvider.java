package yarnternal.lol.core.provider;

public record ClientInfoProvider(
        String clientVersion,
        String clientName,
        String clientBranch
) implements ClientInfo {

    @Override
    public String getFullInfo() {
        return String.format("Welcome to %s! Version: %s, branch: %s", clientName, clientVersion, clientBranch);
    }
}
