package dev.exhq.wedlock;

public record CurrentAuthorization(
        String[] scopes,
        DiscordUser user,
        Application application
) {
}
