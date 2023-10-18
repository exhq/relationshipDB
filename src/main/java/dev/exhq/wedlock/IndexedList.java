package dev.exhq.wedlock;

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

}
