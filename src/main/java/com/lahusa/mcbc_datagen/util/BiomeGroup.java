package com.lahusa.mcbc_datagen.util;

public enum BiomeGroup {
    AQUATIC(1),
    FOREST(1),
    PLAINS(2),
    ARID(3),
    SNOWY(3);

    private final int scaleFactor;

    BiomeGroup(int scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    public int getScaleFactor() {
        return scaleFactor;
    }
}
