package com.lahusa.mcbc_datagen.util;

import com.lahusa.mcbc_datagen.MCBCDataGenMod;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryEntry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.GameMode;
import net.minecraft.world.GameRules;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;

import java.util.*;

public class DataGenerationManager {
    private static final LinkedList<DataGenerationSchedule> schedules;
    private static final Random rand;
    // 10 second delay
    private static final int WORLD_GEN_DELAY_TICKS = 200; // 10s
    private static final int RAND_DELAY_TICKS = 20; // 1s
    private static final int MIN_TOTAL_SCREENSHOTS = 3;
    private static PlayerManager serverPlayerManager;

    static {
        schedules = new LinkedList<>();
        rand = Random.create();
    }

    public static void initialize(PlayerManager playerManager) {
        serverPlayerManager = playerManager;
        System.out.println("Initialized manager");
    }

    public static void schedule(ServerPlayerEntity player, int iterations) {
        schedules.add(new DataGenerationSchedule(player, iterations));
        System.out.println("Added schedule");
    }

    public static void tick() {
        if(serverPlayerManager == null) {
            throw new IllegalStateException("tick was called without previous initialization");
        }

        Iterator<DataGenerationSchedule> scheduleIterator = schedules.iterator();
        while(scheduleIterator.hasNext()) {
            DataGenerationSchedule schedule = scheduleIterator.next();

            // Tick schedule (decrement delays)
            schedule.tick();

            ServerPlayerEntity player = schedule.getPlayer();
            MinecraftServer server = Objects.requireNonNull(player.getServer());

            // Handle cases
            switch(schedule.getState()) {
                case SCHED_INIT -> {
                    unlockAllContent(player, server);
                    setClientResolution(player);
                    schedule.setState(DataGenerationSchedule.State.ITER_INIT);
                }
                case ITER_INIT -> {
                    // Reset player to a clean state
                    cleanPlayerState(player);

                    // Schedule done --> remove schedule & reset player
                    if(schedule.isDone()) {
                        scheduleIterator.remove();
                        System.out.println("Schedule finished and removed");
                    }
                    // Schedule not done --> start next iteration
                    else {
                        // Teleport player
                        randomizePosition(player, schedule);
                        player.clearStatusEffects();

                        System.out.println(
                                "Started iteration (" + (schedule.getElapsedIterations() + 1)
                                        + "/" + schedule.getTotalIterations() + ")"
                        );

                        schedule.startDelay(WORLD_GEN_DELAY_TICKS);
                        schedule.setState(DataGenerationSchedule.State.AWAIT_GEN_DELAY);
                    }
                }
                case AWAIT_GEN_DELAY -> {
                    if(schedule.isDelayElapsed()) {
                        schedule.setState(DataGenerationSchedule.State.RANDOMIZATION);
                    }
                }
                case RANDOMIZATION -> {
                    // Randomize all parameters except position
                    randomizePlayerState(player, schedule);
                    schedule.startDelay(RAND_DELAY_TICKS);
                    System.out.println("Started randomization");

                    schedule.setState(DataGenerationSchedule.State.AWAIT_RAND_DELAY);
                }
                case AWAIT_RAND_DELAY -> {
                    if(schedule.isDelayElapsed()) {
                        // Request screenshot
                        // Get screenshot filename
                        String fileName = getScreenShotFileName(player, schedule.getCapturedScreenShots());

                        // Send force screenshot packet to client
                        PacketByteBuf fileNameBuf = PacketByteBufs.create();
                        fileNameBuf.writeString(fileName);

                        System.out.println("Requested screenshot (Filename: " + fileName + ")");
                        schedule.setState(DataGenerationSchedule.State.AWAIT_SCREENSHOT_CONF);
                        ServerPlayNetworking.send(player, MCBCDataGenMod.FORCE_SCREENSHOT_PACKET_ID, fileNameBuf);
                    }
                }
            }
        }
    }

    public static void clear() {
        schedules.clear();
    }

    public static void removePlayer(ServerPlayerEntity player) {
        schedules.removeIf(schedule -> schedule.getPlayer().equals(player));
    }

    public static void handleScreenShotConfirmation(ServerPlayerEntity player) {
        for(DataGenerationSchedule schedule : schedules) {
            if(schedule.getPlayer() == player && schedule.getState() == DataGenerationSchedule.State.AWAIT_SCREENSHOT_CONF) {
                // Increment screenshot counter
                schedule.setCapturedScreenShots(schedule.getCapturedScreenShots() + 1);

                // Screenshots remain
                if(schedule.getCapturedScreenShots() < schedule.getTotalScreenShots()) {
                    schedule.setState(DataGenerationSchedule.State.RANDOMIZATION);
                    System.out.println("Got screenshot");
                }
                // Screenshots are done
                else {
                    schedule.setState(DataGenerationSchedule.State.ITER_INIT);
                    System.out.println("Got screenshot, ended iteration");
                    schedule.beginNewIteration();
                }
                break;
            }
        }
    }

    public static void handleTeleportConfirmation(ServerPlayerEntity player) {
        for(DataGenerationSchedule schedule : schedules) {
            if(schedule.getPlayer() == player && schedule.getState() == DataGenerationSchedule.State.AWAIT_TP_CONFIRMATION) {
                schedule.setState(DataGenerationSchedule.State.AWAIT_GEN_DELAY);
                schedule.startDelay(WORLD_GEN_DELAY_TICKS);
                System.out.println("Teleport confirmed, started delay");
                break;
            }
        }
    }

    public static boolean containsScheduleForPlayer(ServerPlayerEntity player) {
        for (DataGenerationSchedule schedule : schedules) {
            if (schedule.getPlayer() == player) {
                return true;
            }
        }
        return false;
    }

    private static String getScreenShotFileName(ServerPlayerEntity player, int screenShotIndex) {
        ServerWorld world = player.getWorld();
        BlockPos blockPos = player.getBlockPos();

        // Get biome
        Optional<RegistryKey<Biome>> biomeRegistryKey = world.getBiome(blockPos).getKey();

        if(biomeRegistryKey.isEmpty()) throw new IllegalStateException("BlockPos had no associated Biome");

        // Get biome ID without namespace (e.g. "minecraft:plains" => "plains")
        String biomeID = biomeRegistryKey.get().getValue().getPath();

        return biomeID + "-" + blockPos.getX() + "_" + blockPos.getZ() + "-" + screenShotIndex + ".png";
    }

    public static void cleanPlayerState(ServerPlayerEntity player) {
        // Clear inventory
        player.getInventory().clear();

        // Set gamemode to creative
        player.changeGameMode(GameMode.CREATIVE);

        // Reset armor value
        EntityAttributeInstance armorAttribute = Objects.requireNonNull(player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR));
        armorAttribute.setBaseValue(0);

        // Make HUD visible
        setClientHudHidded(player, false);
    }

    private static void randomizePlayerState(ServerPlayerEntity player, DataGenerationSchedule schedule) {
        randomizeInventory(player);
        randomizeGameMode(player);
        randomizeVisualStats(player);
        randomizeExperience(player);
        randomizeTimeAndWeather(player);
        randomizeHudVisibility(player);
        randomizeRotation(player, schedule);
    }

    private static void randomizePosition(ServerPlayerEntity player, DataGenerationSchedule schedule) {
        ServerWorld world = player.getWorld();

        // Randomize position until we find a classifiable biome
        for (int tries = 0; true; tries++) {
            int x = rand.nextInt(10000000);
            int z = rand.nextInt(10000000);
            int yaw = rand.nextInt(360) - 180;
            int pitch = rand.nextBetween(-30, 45);

            // Start force loading (Make data available before tp)
            world.setChunkForced(ChunkSectionPos.getSectionCoord(x), ChunkSectionPos.getSectionCoord(z), true);

            // Get top non-leaf block
            int y = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);

            // Stop force loading
            world.setChunkForced(ChunkSectionPos.getSectionCoord(x), ChunkSectionPos.getSectionCoord(z), false);

            // Get biome
            Optional<RegistryKey<Biome>> biomeRegistryKey = world.getBiome(new BlockPos(x, y, z)).getKey();
            if(biomeRegistryKey.isEmpty()) throw new IllegalStateException("BlockPos had no associated Biome");

            // Check if the biome is part of our biome groupings
            if (BiomeDistribution.contains(biomeRegistryKey.get())) {
                BiomeGroup biomeGroup = BiomeDistribution.getGroup(biomeRegistryKey.get());

                // Get the scale factor to determine how many screenshots are needed for this biome
                int scaleFactor = biomeGroup.getScaleFactor();
                int addedFactor = biomeGroup.addedFactor();
                int adjustedScaleFactor = scaleFactor + addedFactor;

                // We can skip if the adjustedScaleFactor is 0
                if (adjustedScaleFactor == 0) continue;

                schedule.setTotalScreenShots(MIN_TOTAL_SCREENSHOTS * adjustedScaleFactor);

                System.out.println("Classifiable biome found: " + biomeRegistryKey.get().getValue().getPath() + " | " + biomeGroup.name() + ", scale factor: " + scaleFactor + " (+ " + addedFactor + ")");
                System.out.println("Biomes skipped: " + tries); // Either unclassifiable or an adjustedScaleFactor of 0

                // TP
                player.teleport(world, x, y , z, yaw, pitch);
                break;
            }
        }
    }

    private static void randomizeRotation(ServerPlayerEntity player, DataGenerationSchedule schedule) {
        ServerWorld world = player.getWorld();

        float yaw = player.getYaw() + 360.f / schedule.getTotalScreenShots();
        if(yaw >= 180) yaw -= 360;
        float pitch = rand.nextBetween(-30, 45);

        // TP
        player.teleport(world, player.getX(), player.getY(), player.getZ(), yaw, pitch);
    }

    private static void unlockAllContent(ServerPlayerEntity player, MinecraftServer server) {
        // Deactivate advancement announcements in chat
        server.getGameRules().get(GameRules.ANNOUNCE_ADVANCEMENTS).set(false, server);

        // Grant all recipes
        player.unlockRecipes(server.getRecipeManager().values());

        // Grant all advancements
        Collection<Advancement> advancements = server.getAdvancementLoader().getAdvancements();
        for(Advancement advancement : advancements) {
            AdvancementProgress progress = player.getAdvancementTracker().getProgress(advancement);
            for(String criterion : progress.getUnobtainedCriteria()) {
                player.getAdvancementTracker().grantCriterion(advancement, criterion);
            }
        }
    }

    private static void randomizeInventory(ServerPlayerEntity player) {
        PlayerInventory inventory = player.getInventory();

        inventory.clear();

        // Fill individual hotbar slots
        for(int i = 0; i < 9; i++) {
            // Make some slots empty
            if(rand.nextInt(3)==0) continue;

            inventory.setStack(i, getRandomizedItemStack());
        }

        // Occasionally fill offhand slot with random item
        if(rand.nextInt(3)==0) {
            inventory.setStack(PlayerInventory.OFF_HAND_SLOT, getRandomizedItemStack());
        }

        // Set new selected slot
        int newSelectedSlot = rand.nextInt(9);
        inventory.selectedSlot = newSelectedSlot;

        // Send slot selection update packet
        PacketByteBuf slotPacketByteBuf = PacketByteBufs.create();
        slotPacketByteBuf.writeInt(newSelectedSlot);
        ServerPlayNetworking.send(player, MCBCDataGenMod.INVENTORY_SLOT_CHANGE_PACKET_ID, slotPacketByteBuf);
    }

    private static void randomizeTimeAndWeather(ServerPlayerEntity player) {
        boolean raining = rand.nextInt(5) == 0;
        boolean thundering = raining && rand.nextBoolean();
        ServerWorld world = player.getWorld();
        world.setWeather(0, 0, raining, thundering);

        long time = rand.nextInt(24000);
        world.setTimeOfDay(time);

        // If mob in lower end burning timeframe, remove it
        if(time < 12575) {
            for (Entity entity : world.iterateEntities()) {
                if (entity instanceof HostileEntity) {
                    entity.remove(Entity.RemovalReason.DISCARDED);
                }
            }
        }


    }

    private static void randomizeGameMode(ServerPlayerEntity player) {
        int gameModeRoll = rand.nextInt(6);
        GameMode gameMode = switch(gameModeRoll) {
            case 0, 1, 2 -> GameMode.CREATIVE;
            case 3, 4 -> GameMode.SURVIVAL;
            case 5 -> GameMode.ADVENTURE;
            default -> throw new IllegalStateException("Unexpected value: " + gameModeRoll);
        };

        player.changeGameMode(gameMode);
    }

    private static void randomizeVisualStats(ServerPlayerEntity player) {
        HungerManager hungerManager = player.getHungerManager();

        // Set HP
        player.setHealth(rand.nextBetween(1,20));

        // Set hunger bars
        hungerManager.setFoodLevel(rand.nextBetween(0,20));

        // Randomize (or reset) armor value
        EntityAttributeInstance armorAttribute = Objects.requireNonNull(player.getAttributeInstance(EntityAttributes.GENERIC_ARMOR));
        armorAttribute.setBaseValue(rand.nextBoolean() ? rand.nextBetween(1,20) : 0);
    }

    private static void randomizeHudVisibility(ServerPlayerEntity player) {
        setClientHudHidded(player, rand.nextInt(4) == 0);
    }

    private static void randomizeExperience(ServerPlayerEntity player) {
        player.setExperienceLevel(rand.nextBetween(0, 100));
        player.setExperiencePoints(rand.nextBetween(0, player.getNextLevelExperience()));
    }

    private static ItemStack getRandomizedItemStack() {
        // Get random item
        Optional<RegistryEntry<Item>> randomItemOpt = Registry.ITEM.getRandom(rand);
        if(randomItemOpt.isEmpty()) throw new IllegalStateException("Random item was empty");
        Item randomItem = randomItemOpt.get().value();

        ItemStack stack = new ItemStack(randomItem, rand.nextBetween(1, randomItem.getMaxCount()));

        // Randomly damage stack
        if(randomItem.isDamageable() && rand.nextInt(2)!=0) {
            stack.setDamage(rand.nextBetween(0, randomItem.getMaxDamage()));
        }

        return stack;
    }

    private static void setClientHudHidded(ServerPlayerEntity player, boolean hudHidden) {
        PacketByteBuf hudHiddenPacketByteBuf = PacketByteBufs.create();
        hudHiddenPacketByteBuf.writeBoolean(hudHidden);
        ServerPlayNetworking.send(player, MCBCDataGenMod.SET_HUD_HIDDEN_PACKET_ID, hudHiddenPacketByteBuf);
    }

    private static void setClientResolution(ServerPlayerEntity player) {
        ServerPlayNetworking.send(player, MCBCDataGenMod.FORCE_RESOLUTION_CHANGE_PACKET_ID, PacketByteBufs.create());
    }
}
