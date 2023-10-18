package dev.exhq.wedlock;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Function;

public class Index<T, E> {
    private final List<Function<T, E>> keys;
    private Map<E, Set<T>> lookup = new HashMap<>();

    @SafeVarargs
    public Index(@NotNull Function<T, E>... keys) {
        this(Arrays.asList(keys));
    }

    public Index(@NotNull List<@NotNull Function<T, E>> keys) {
        this.keys = keys;
    }

    public void reindex(List<T> ts) {
        lookup.clear();
        for (T t : ts) {
            insertObject(t);
        }
    }

    public void removeObject(T t) {
        for (Function<T, E> key : keys) {
            var index = key.apply(t);
            var objects = lookup.get(index);
            if (objects != null)
                objects.remove(t);
        }
    }

    public void insertObject(T t) {
        for (Function<T, E> key : keys) {
            var index = key.apply(t);
            var objects = lookup.computeIfAbsent(index, ignored -> new HashSet<>());
            objects.add(t);
        }
    }

    public @NotNull Collection<T> get(E key) {
        return lookup.getOrDefault(key, Collections.emptySet());
    }
}
