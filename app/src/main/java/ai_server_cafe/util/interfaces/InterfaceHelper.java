package ai_server_cafe.util.interfaces;

import javax.annotation.Nonnull;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

public class InterfaceHelper {
    @Nonnull
    public static <T> Comparator<T> getComparator(IFuncParam2<Boolean, T, T> func) {
        return new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                return func.function(o1, o2) ? -1 : 1;
            }
        };
    }

    @Nonnull
    public static <T> Predicate<T> getPredicate(IFuncParam1<Boolean, T> func) {
        return new Predicate<T>() {
            @Override
            public boolean test(T t) {
                return func.function(t);
            }
        };
    }

    @Nonnull
    public static <K, V> Optional<K> getKey(@Nonnull Map<K, V> map, V v) {
        for (Map.Entry<K, V> entry : map.entrySet()) {
            if (entry.getValue().equals(v))
                return Optional.of(entry.getKey());
        }
        return Optional.empty();
    }
}
