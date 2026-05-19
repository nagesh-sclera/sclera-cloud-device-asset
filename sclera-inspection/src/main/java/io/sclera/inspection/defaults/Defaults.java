package io.sclera.inspection.defaults;

import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Defaults {
    private Defaults() {}

    public static final String NULL_STRING = null;
    public static final Integer ZERO = 0;
    public static final Long ZERO_LONG = 0L;
    public static final Boolean FALSE = Boolean.FALSE;

    public static <T> List<T> emptyList() { return List.of(); }
    public static <T> Set<T> emptySet() { return Set.of(); }
    public static <K, V> Map<K, V> emptyMap() { return Map.of(); }
}
