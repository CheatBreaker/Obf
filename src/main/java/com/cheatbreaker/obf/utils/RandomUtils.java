package com.cheatbreaker.obf.utils;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class RandomUtils {
    public static <T> List<T> swap(Random random, T a, T b) {
        if (random.nextBoolean()) {
            return Arrays.asList(a, b);
        } else {
            return Arrays.asList(b, a);
        }
    }

    public static <T> T choice(Random random, List<T> items) {
        return items.get(random.nextInt(items.size()));
    }
}
