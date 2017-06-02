package com.thestudnet.twicandroidplugin.utils;

import java.util.Random;

/**
 * INTERACTIVE LAYER
 * Created by Baptiste PHILIBERT on 05/05/2017.
 */

public class RandomInt extends Random {

    public RandomInt() {}
    public RandomInt(int seed) { super(seed); }

    public int nextNonNegative() {
        return next(Integer.SIZE - 1);
    }

}
