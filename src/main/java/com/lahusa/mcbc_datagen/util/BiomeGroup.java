package com.lahusa.mcbc_datagen.util;

import net.minecraft.util.math.random.Random;

public enum BiomeGroup {
    AQUATIC(0, 83),
    FOREST(0, 95),
    PLAINS(1, 95),
    ARID(2, 70),
    SNOWY(2, 97);

    private final int scaleFactor;
    private final int percentToNextFactor;
    private static final Random rand;

    static {
        rand = Random.create();
    }

    BiomeGroup(int scaleFactor, int percentToNextFactor) {
        this.scaleFactor = scaleFactor;
        this.percentToNextFactor = percentToNextFactor;
    }

    public int getScaleFactor() {
        return scaleFactor;
    }

    public int addedFactor() {
        if (rand.nextInt(100) < percentToNextFactor) {
            return 1;
        }
        return 0;
    }
}
