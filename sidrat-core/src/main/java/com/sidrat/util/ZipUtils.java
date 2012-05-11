package com.sidrat.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

public class ZipUtils {
    public static <A,B> List<Pair<A,B>> zip(A[] as, B[] bs) {
        return zip(Lists.newArrayList(as), Lists.newArrayList(bs));
    }

    public static <A,B> List<Pair<A,B>> zip(List<A> as, List<B> bs) {
        if (as.size() != bs.size()) {
            throw new IllegalArgumentException("the two lists must be ");
        }
        List<Pair<A,B>> pairs = new ArrayList<Pair<A,B>>();
        for (int idx = 0; idx < as.size(); idx++) {
            A a = as.get(idx);
            B b = bs.get(idx);
            pairs.add(new Pair<A,B>(a,b));
        }
        return pairs;
    }

    public static <A,B> Map<A,B> zipAsMap(A[] as, B[] bs) {
        return zipAsMap(Lists.newArrayList(as), Lists.newArrayList(bs));
    }
    
    public static <A,B> Map<A,B> zipAsMap(List<A> as, List<B> bs) {
        if (as.size() != bs.size()) {
            throw new IllegalArgumentException("the two lists must be ");
        }
        Map<A,B> map = new HashMap<A,B>();
        for (int idx = 0; idx < as.size(); idx++) {
            A a = as.get(idx);
            B b = bs.get(idx);
            map.put(a,b);
        }
        return map;
    }
}