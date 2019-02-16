package utils;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Utils {
    @SafeVarargs
    public static <K> List<K> mutableListOf(K... items) {
        return new ArrayList<>(Arrays.asList(items));
    }

    public static <K> List<K> addAsFirst(List<K> list, K item) {
        list.add(0, item);
        return list;
    }

    public static <K, V> String toString(Map<K, V> map, Function<K, String> key, Function<V, String> value) {
        return map.entrySet()
                .stream()
                .map(entry -> key.apply(entry.getKey()) + " - " + value.apply(entry.getValue()))
                .collect(Collectors.joining(", "));
    }

    @NotNull
    public static <K> K last(@NotNull List<K> list) {
        return list.get(list.size() - 1);
    }

    @SafeVarargs
    public static <K> Set<K> setOf(K... items) {
        return new HashSet<>(Arrays.asList(items));
    }

    public static boolean isPowerOfTwo(int number) {
        return number > 0 && ((number & (number - 1)) == 0);
    }
}
