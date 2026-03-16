package ru.rbpo.backend.signature;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/** RFC 8785 (JCS): один и тот же JSON → одна и та же строка (иначе подпись не сойдётся). */
public final class JsonCanonicalizer {

    private JsonCanonicalizer() { }

    public static String toCanonicalString(Object value) {
        StringBuilder sb = new StringBuilder();
        appendValue(value, sb);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private static void appendValue(Object value, StringBuilder sb) {
        if (value == null) {
            sb.append("null");
            return;
        }
        if (value instanceof Boolean) {
            sb.append((Boolean) value ? "true" : "false");
            return;
        }
        if (value instanceof Number) {
            appendNumber((Number) value, sb);
            return;
        }
        if (value instanceof String) {
            appendString((String) value, sb);
            return;
        }
        if (value instanceof Map) {
            appendObject((Map<String, Object>) value, sb);
            return;
        }
        if (value instanceof List) {
            appendArray((List<?>) value, sb);
            return;
        }
        if (value instanceof java.time.Instant) {
            appendString(value.toString(), sb);
            return;
        }
        throw new IllegalArgumentException("Неподдерживаемый тип для канонизации: " + value.getClass().getName());
    }

    private static void appendNumber(Number n, StringBuilder sb) {
        if (n instanceof Long || n instanceof Integer || n instanceof Short || n instanceof Byte) {
            sb.append(n.longValue());
            return;
        }
        double d = n.doubleValue();
        if (Double.isNaN(d) || Double.isInfinite(d)) {
            throw new IllegalArgumentException("NaN и Infinity не допускаются в JSON (RFC 8785)");
        }
        long l = (long) d;
        if (l == d && l >= -9007199254740991L && l <= 9007199254740991L) {
            sb.append(l);
        } else {
            sb.append(d);
        }
    }

    private static void appendString(String s, StringBuilder sb) {
        sb.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '\b' -> sb.append("\\b");
                case '\t' -> sb.append("\\t");
                case '\n' -> sb.append("\\n");
                case '\f' -> sb.append("\\f");
                case '\r' -> sb.append("\\r");
                default -> {
                    if (c >= 0x00 && c <= 0x1F) {
                        sb.append("\\u").append(String.format("%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        sb.append('"');
    }

    private static void appendObject(Map<String, Object> map, StringBuilder sb) {
        sb.append('{');
        TreeMap<String, Object> sorted = new TreeMap<>(map);
        boolean first = true;
        for (Map.Entry<String, Object> e : sorted.entrySet()) {
            if (!first) sb.append(',');
            first = false;
            appendString(e.getKey(), sb);
            sb.append(':');
            appendValue(e.getValue(), sb);
        }
        sb.append('}');
    }

    private static void appendArray(List<?> list, StringBuilder sb) {
        sb.append('[');
        for (int i = 0; i < list.size(); i++) {
            if (i > 0) sb.append(',');
            appendValue(list.get(i), sb);
        }
        sb.append(']');
    }
}
