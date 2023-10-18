package dev.exhq.wedlock;

public record DiscordUser(
        String id,
        String username,
        String discriminator
) {
}
