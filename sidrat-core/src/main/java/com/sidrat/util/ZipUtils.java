package com.sidrat.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ZipUtils {
    private static <E> List<E> newArrayList(E... t) {
        if (t == null || t.length == 0) {
            return Collections.emptyList();
        }
        List<E> list = new ArrayList<E>(t.length);
        Collections.addAll(list, t);
        return list;
    }

    public static <A, B> List<Pair<A, B>> zip(A[] as, B[] bs) {
        return zip(newArrayList(as), newArrayList(bs));
    }

    public static <A, B> List<Pair<A, B>> zip(List<A> as, List<B> bs) {
        if (as.size() != bs.size()) {
            throw new IllegalArgumentException("the two lists must be ");
        }
        List<Pair<A, B>> pairs = new ArrayList<Pair<A, B>>();
        for (int idx = 0; idx < as.size(); idx++) {
            A a = as.get(idx);
            B b = bs.get(idx);
            pairs.add(new Pair<A, B>(a, b));
        }
        return pairs;
    }

    public static <A, B> Map<A, B> zipAsMap(A[] as, B[] bs) {
        return zipAsMap(newArrayList(as), newArrayList(bs), true);
    }

    public static <A, B> Map<A, B> zipAsMap(A[] as, B[] bs, boolean sameSize) {
        return zipAsMap(newArrayList(as), newArrayList(bs), sameSize);
    }

    public static <A, B> Map<A, B> zipAsMap(List<A> as, List<B> bs) {
        return zipAsMap(as, bs, true);
    }

    public static <A, B> Map<A, B> zipAsMap(List<A> as, List<B> bs, boolean sameSize) {
        if (as.size() != bs.size() && sameSize) {
            throw new IllegalArgumentException("the two lists must be equal in size");
        }
        int size = as.size();
        if (!sameSize) {
            size = Math.min(as.size(), bs.size());
        }
        Map<A, B> map = new HashMap<A, B>();
        for (int idx = 0; idx < size; idx++) {
            A a = as.get(idx);
            B b = bs.get(idx);
            map.put(a, b);
        }
        return map;
    }
}
