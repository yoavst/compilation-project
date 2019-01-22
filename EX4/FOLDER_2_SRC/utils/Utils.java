package utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Utils {
    @SafeVarargs
    public static <K> List<K> mutableListOf(K... items) {
        return new ArrayList<>(Arrays.asList(items));
    }

    public static <K> List<K> addAsFirst(List<K> list, K item) {
        list.add(0, item);
        return list;
    }
}
