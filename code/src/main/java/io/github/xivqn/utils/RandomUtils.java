package io.github.xivqn.utils;

import java.util.*;
import org.apache.commons.math3.random.MersenneTwister;
import org.apache.commons.math3.random.RandomAdaptor;


public class RandomUtils {

    private static final long SEED = 19030701L;

    private static final MersenneTwister random = new MersenneTwister(SEED);

    private static final RandomAdaptor adaptor = new RandomAdaptor(random);

    private static final MersenneTwister[] cache = new MersenneTwister[Math.max(19030701, ArgsUtils.getSimulations())];

    public static MersenneTwister getRandom() {
        return random;
    }

    public static void shuffleList(List<?> list) {
        Collections.shuffle(list, adaptor);
    }

    public static void setSeed(long seed) {
        random.setSeed(seed);
    }

    public static MersenneTwister getNew(long seed) {
        int seedInt = Math.toIntExact(seed);

        if (seed < cache.length && ArgsUtils.isCacheRandom()) {
            if (cache[seedInt] == null) cache[seedInt] = new MersenneTwister(seed);
            return cache[seedInt];
        } else return new MersenneTwister(seed);
    }

    public static MersenneTwister getNew() {
        return getNew(SEED);
    }

}
