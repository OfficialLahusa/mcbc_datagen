package com.lahusa.mcbc_datagen.util;

import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

import java.util.HashMap;

public class BiomeDistribution {

    private static final HashMap<RegistryKey<Biome>, BiomeGroup> classifiableBiomes;

    static {
        classifiableBiomes = new HashMap<>();

        // Add all classifiable biomes to the hashmap
        /* AQUATIC */
        classifiableBiomes.put(BiomeKeys.COLD_OCEAN, BiomeGroup.AQUATIC);
        classifiableBiomes.put(BiomeKeys.DEEP_COLD_OCEAN, BiomeGroup.AQUATIC);
        classifiableBiomes.put(BiomeKeys.DEEP_FROZEN_OCEAN, BiomeGroup.AQUATIC);
        classifiableBiomes.put(BiomeKeys.DEEP_LUKEWARM_OCEAN, BiomeGroup.AQUATIC);
        classifiableBiomes.put(BiomeKeys.DEEP_OCEAN, BiomeGroup.AQUATIC);
        classifiableBiomes.put(BiomeKeys.LUKEWARM_OCEAN, BiomeGroup.AQUATIC);
        classifiableBiomes.put(BiomeKeys.OCEAN, BiomeGroup.AQUATIC);
        classifiableBiomes.put(BiomeKeys.RIVER, BiomeGroup.AQUATIC);
        classifiableBiomes.put(BiomeKeys.WARM_OCEAN, BiomeGroup.AQUATIC);

        /* SNOWY */
        classifiableBiomes.put(BiomeKeys.ICE_SPIKES, BiomeGroup.SNOWY);
        classifiableBiomes.put(BiomeKeys.FROZEN_OCEAN, BiomeGroup.SNOWY);
        classifiableBiomes.put(BiomeKeys.FROZEN_PEAKS, BiomeGroup.SNOWY);
        classifiableBiomes.put(BiomeKeys.FROZEN_RIVER, BiomeGroup.SNOWY);
        classifiableBiomes.put(BiomeKeys.GROVE, BiomeGroup.SNOWY);
        classifiableBiomes.put(BiomeKeys.JAGGED_PEAKS, BiomeGroup.SNOWY);
        classifiableBiomes.put(BiomeKeys.SNOWY_BEACH, BiomeGroup.SNOWY);
        classifiableBiomes.put(BiomeKeys.SNOWY_PLAINS, BiomeGroup.SNOWY);
        classifiableBiomes.put(BiomeKeys.SNOWY_SLOPES, BiomeGroup.SNOWY);
        classifiableBiomes.put(BiomeKeys.SNOWY_TAIGA, BiomeGroup.SNOWY);

        /* ARID */
        classifiableBiomes.put(BiomeKeys.BADLANDS, BiomeGroup.ARID);
        classifiableBiomes.put(BiomeKeys.BEACH, BiomeGroup.ARID);
        classifiableBiomes.put(BiomeKeys.DESERT, BiomeGroup.ARID);
        classifiableBiomes.put(BiomeKeys.ERODED_BADLANDS, BiomeGroup.ARID);
        classifiableBiomes.put(BiomeKeys.SAVANNA, BiomeGroup.ARID);
        classifiableBiomes.put(BiomeKeys.SAVANNA_PLATEAU, BiomeGroup.ARID);
        classifiableBiomes.put(BiomeKeys.WINDSWEPT_SAVANNA, BiomeGroup.ARID);
        classifiableBiomes.put(BiomeKeys.WOODED_BADLANDS, BiomeGroup.ARID);

        /* FOREST */
        classifiableBiomes.put(BiomeKeys.BAMBOO_JUNGLE, BiomeGroup.FOREST);
        classifiableBiomes.put(BiomeKeys.BIRCH_FOREST, BiomeGroup.FOREST);
        classifiableBiomes.put(BiomeKeys.DARK_FOREST, BiomeGroup.FOREST);
        classifiableBiomes.put(BiomeKeys.FLOWER_FOREST, BiomeGroup.FOREST);
        classifiableBiomes.put(BiomeKeys.FOREST, BiomeGroup.FOREST);
        classifiableBiomes.put(BiomeKeys.JUNGLE, BiomeGroup.FOREST);
        classifiableBiomes.put(BiomeKeys.MANGROVE_SWAMP, BiomeGroup.FOREST);
        classifiableBiomes.put(BiomeKeys.MUSHROOM_FIELDS, BiomeGroup.FOREST);
        classifiableBiomes.put(BiomeKeys.OLD_GROWTH_BIRCH_FOREST, BiomeGroup.FOREST);
        classifiableBiomes.put(BiomeKeys.OLD_GROWTH_PINE_TAIGA, BiomeGroup.FOREST);
        classifiableBiomes.put(BiomeKeys.OLD_GROWTH_SPRUCE_TAIGA, BiomeGroup.FOREST);
        classifiableBiomes.put(BiomeKeys.SPARSE_JUNGLE, BiomeGroup.FOREST);
        classifiableBiomes.put(BiomeKeys.SWAMP, BiomeGroup.FOREST);
        classifiableBiomes.put(BiomeKeys.TAIGA, BiomeGroup.FOREST);
        classifiableBiomes.put(BiomeKeys.WINDSWEPT_FOREST, BiomeGroup.FOREST);

        /* PLAINS */
        classifiableBiomes.put(BiomeKeys.MEADOW, BiomeGroup.PLAINS);
        classifiableBiomes.put(BiomeKeys.PLAINS, BiomeGroup.PLAINS);
        classifiableBiomes.put(BiomeKeys.STONY_PEAKS, BiomeGroup.PLAINS);
        classifiableBiomes.put(BiomeKeys.STONY_SHORE, BiomeGroup.PLAINS);
        classifiableBiomes.put(BiomeKeys.SUNFLOWER_PLAINS, BiomeGroup.PLAINS);
        classifiableBiomes.put(BiomeKeys.WINDSWEPT_GRAVELLY_HILLS, BiomeGroup.PLAINS);
        classifiableBiomes.put(BiomeKeys.WINDSWEPT_HILLS, BiomeGroup.PLAINS);
    }

    public static boolean contains(RegistryKey<Biome> biomeRegistryKey) {
        return classifiableBiomes.containsKey(biomeRegistryKey);
    }

    public static BiomeGroup getGroup(RegistryKey<Biome> biomeRegistryKey) {
        return classifiableBiomes.get(biomeRegistryKey);
    }

}
