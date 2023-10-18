package dev.exhq.wedlock;

import com.google.gson.Gson;
import io.javalin.json.JsonMapper;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;

public record GsonMapper(
        Gson gson
) implements JsonMapper {
    @NotNull
    @Override
    public <T> T fromJsonString(@NotNull String json, @NotNull Type targetType) {
        return gson.fromJson(json, targetType);
    }

    @NotNull
    @Override
    public String toJsonString(@NotNull Object obj, @NotNull Type type) {
        return gson.toJson(obj, type);
    }

    public static Gson defaultGson = new Gson();

    public static @NotNull GsonMapper createGsonMapper() {
        return new GsonMapper(defaultGson);
    }
}
