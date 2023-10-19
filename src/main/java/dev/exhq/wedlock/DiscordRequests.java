package dev.exhq.wedlock;

import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Objects;

public class DiscordRequests {

    public static <T> HttpResponse.@NotNull BodyHandler<T> getJsonHandler(@NotNull Class<T> tClass) {
        return responseInfo -> HttpResponse.BodySubscribers.mapping(HttpResponse.BodySubscribers.ofInputStream(),
                inp -> GsonMapper.defaultGson.fromJson(new InputStreamReader(inp), tClass));
    }

    public static @NotNull CurrentAuthorization requireAuthentication(@NotNull Context context) {
        var auth = selfAuthenticate(context.header("Authorization"));
        if (auth == null) {
            throw new AuthenticationMissingException();
        }
        return auth;
    }

    public static @Nullable CurrentAuthorization selfAuthenticate(@Nullable String token) {
        if (token == null) return null;
        var httpClient = HttpClient.newHttpClient();
        try {
            var response = httpClient.send(HttpRequest.newBuilder()
                    .GET()
                    .uri(new URI("https://discord.com/api/v10/oauth2/@me"))
                    .header("Authorization", "Bearer " + token)
                    .build(), getJsonHandler(CurrentAuthorization.class));
            if (response.statusCode() != 200)
                return null;
            var data = response.body();
            if (!Objects.equals(data.application().id(), "394100837032394763")) {
                System.out.println("Logged in as " + data.application().id() + " expected 394100837032394763");
                return null;
            }
            return data;
        } catch (IOException | InterruptedException | URISyntaxException e) {
            return null;
        }
    }
}
