package com.sidrat.util;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Strings {
    public static String combine(List<?> values) {
        return combine(values, ',');
    }

    public static String combine(List<?> values, char separator) {
        return combine(values.toArray(), separator);
    }

    public static String combine(Object[] arr) {
        return combine(arr, ',');
    }

    public static String combine(Object[] arr, char separator) {
        if (arr == null) {
            return "null";
        }
        StringBuilder sb = new StringBuilder();
        for (Object v : arr) {
            if (v == null) {
                sb.append("null");
            } else {
                sb.append(v.toString());
            }
            sb.append(separator);
        }
        sb.deleteCharAt(sb.length() - 1);
        return sb.toString();
    }

    public static String[] captures(String input, Pattern regex) {
        Matcher matcher = regex.matcher(input);
        if (!matcher.matches())
            return null;
        int groupCount = matcher.groupCount() + 1;
        String[] captures = new String[groupCount];
        for (int i = 0; i < groupCount; i++) {
            captures[i] = matcher.group(i);
        }
        return captures;
    }
}
