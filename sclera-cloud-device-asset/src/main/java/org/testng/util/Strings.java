package org.testng.util;
/** STUB: testng not on classpath, minimal Strings shim */
public final class Strings {
    private Strings() {}
    public static boolean isNullOrEmpty(String s) { return s == null || s.isEmpty(); }
    public static boolean isNotNullAndNotEmpty(String s) { return !isNullOrEmpty(s); }
}
