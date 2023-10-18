package dev.exhq.wedlock;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;

public class CollectionHelper {
    public static <T> @Nullable T getFirstOrNull(@NotNull Iterable<T> ts) {
        Iterator<T> iterator = ts.iterator();
        if (iterator.hasNext())
            return iterator.next();
        return null;
    }
}
