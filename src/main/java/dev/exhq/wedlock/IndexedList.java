package dev.exhq.wedlock;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

public class IndexedList<T> {

    private final List<Index<T, ?>> indexes;
    private final List<T> data;

    @SafeVarargs
    public IndexedList(List<T> data, Index<T, ?>... indexes) {
        this(data, Arrays.asList(indexes));
    }

    public IndexedList(List<T> data, List<Index<T, ?>> indexes) {
        this.indexes = indexes;
        this.data = data;
    }

    public void add(T t) {
        for (Index<T, ?> index : indexes) {
            index.insertObject(t);
        }
        data.add(t);
    }

    public void remove(T t) {
        for (Index<T, ?> index : indexes) {
            index.removeObject(t);
        }
        data.remove(t);
    }

    public void reindex() {
        for (Index<T, ?> index : indexes) {
            index.reindex(data);
        }
    }

    public void deserialize(Gson gson, JsonElement savedData, Class<T> componentClass) {
        data.clear();
        if (savedData instanceof JsonArray)
            data.addAll(gson.fromJson(savedData, new ParameterizedType() {

                @Override
                public Type[] getActualTypeArguments() {
                    return new Type[]{componentClass};
                }

                @Override
                public Type getRawType() {
                    return List.class;
                }

                @Override
                public Type getOwnerType() {
                    return getClass();
                }
            }));
        reindex();
    }

    public JsonElement serialize(Gson gson) {
        return gson.toJsonTree(data);
    }
}
