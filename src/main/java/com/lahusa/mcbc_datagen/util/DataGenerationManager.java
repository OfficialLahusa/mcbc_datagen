package com.lahusa.mcbc_datagen.util;

import com.lahusa.mcbc_datagen.MCBCDataGenMod;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributes;
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
    private static final int DELAY_TICKS = 240;
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

            // Tick schedule
            schedule.tick();

            ServerPlayerEntity player = schedule.getPlayer();
            MinecraftServer server = Objects.requireNonNull(player.getServer());

            // All iterations done
            if(schedule.isDone()) {
                scheduleIterator.remove();

                // Reset player to a clean state
                cleanPlayerState(player);

                System.out.println("Schedule finished and removed");
                continue;
            }

            // Is first iteration
            if(schedule.getElapsedIterations() == 0 && schedule.isIterationStartRequired()) {
                // Unlock all content
                unlockAllContent(player, server);
                setClientResolution(player);
                System.out.println("Unlocked player content");
            }

            // Iteration has to be randomized
            if(schedule.isIterationStartRequired()) {
                // Randomize state (Pos, Inv, ...)
                randomizePlayerState(player);

                // Start iteration
                schedule.startIteration();

                System.out.println(
                        "Started iteration (" + (schedule.getElapsedIterations() + 1)
                                + "/" + schedule.getTotalIterations() + ")"
                );
            }
            // On TP confirmation
            else if(schedule.isTeleportConfirmed() && !schedule.hasDelayStarted()) {
                // Start
                schedule.startDelay(DELAY_TICKS);

                System.out.println("Teleport confirmed, started delay");
            }
            // Screenshot delay already passed
            else if(schedule.isDelayElapsed()){
                // Screenshot wasn't requested yet
                if(!schedule.isScreenShotRequested()) {
                    // Get screenshot filename
                    String fileName = getScreenShotFileName(player);

                    // Send force screenshot packet to client
                    PacketByteBuf fileNameBuf = PacketByteBufs.create();
                    fileNameBuf.writeString(fileName);
                    ServerPlayNetworking.send(player, MCBCDataGenMod.FORCE_SCREENSHOT_PACKET_ID, fileNameBuf);

                    schedule.setScreenShotRequested(true);
                    System.out.println("Requested screenshot (Filename: " + fileName + ")");
                }
                // Screenshot was already confirmed by client
                else if(schedule.isScreenShotConfirmed()) {
                    // Start next iteration
                    schedule.beginNewIteration();
                    System.out.println("Got screenshot, ended iteration");
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
            if(schedule.getPlayer() == player) {
                schedule.confirmScreenShot();
            }
        }
    }

    public static void handleTeleportConfirmation(ServerPlayerEntity player) {
        for(DataGenerationSchedule schedule : schedules) {
            if(schedule.getPlayer() == player) {
                schedule.confirmTeleport();
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

    private static String getScreenShotFileName(ServerPlayerEntity player) {
        ServerWorld world = player.getWorld();
        BlockPos blockPos = player.getBlockPos();

        // Get biome
        Optional<RegistryKey<Biome>> biomeRegistryKey = world.getBiome(blockPos).getKey();

        if(biomeRegistryKey.isEmpty()) throw new IllegalStateException("BlockPos had no associated Biome");

        // Get biome ID without namespace (e.g. "minecraft:plains" => "plains")
        String biomeID = biomeRegistryKey.get().getValue().getPath();

        return biomeID + "-" + blockPos.getX() + "_" + blockPos.getZ() + ".png";
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

    private static void randomizePlayerState(ServerPlayerEntity player) {
        randomizeInventory(player);
        randomizeGameMode(player);
        randomizeVisualStats(player);
        randomizeExperience(player);
        randomizeTimeAndWeather(player);
        randomizeHudVisibility(player);
        randomizePosition(player);
    }

    private static void randomizePosition(ServerPlayerEntity player) {
        ServerWorld world = player.getWorld();

        int x = rand.nextInt(10000000);
        int z = rand.nextInt(10000000);
        int yaw = rand.nextInt(360) - 180;
        int pitch = rand.nextBetween(-30, 30);

        // Start force loading (Make data available before tp)
        world.setChunkForced(ChunkSectionPos.getSectionCoord(x), ChunkSectionPos.getSectionCoord(z), true);

        // Get top non-leaf block
        int y = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);

        // Stop force loading
        world.setChunkForced(ChunkSectionPos.getSectionCoord(x), ChunkSectionPos.getSectionCoord(z), false);

        // TP
        player.teleport(world, x, y , z, yaw, pitch);
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
        player.getWorld().setWeather(0, 0, raining, thundering);
        player.getWorld().setTimeOfDay(rand.nextInt(24000));
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
