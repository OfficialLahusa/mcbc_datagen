package com.lahusa.mcbc_datagen.command;

import com.lahusa.mcbc_datagen.MCBCDataGenMod;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.entity.player.HungerManager;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
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

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static net.minecraft.server.command.CommandManager.literal;

public class DataGenCommand {

    private static final Random rand;

    static {
        rand = Random.create();
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("datagen").executes(
                context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if(player == null) {
                        throw new SimpleCommandExceptionType(Text.literal("This command has to be executed by a player")).create();
                    }

                    MinecraftServer server = Objects.requireNonNull(player.getServer());

                    // Unlock all content
                    unlockAllContent(player, server);

                    // Randomize state
                    randomizeHotbar(player);
                    randomizeGameMode(player);
                    randomizeHPandHunger(player);
                    randomizeTimeAndWeather(player);

                    // Teleport player and get biome identifier without namespace
                    String biome = randomizePosition(player);

                    // Wait 10s and save screenshot
                    ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);

                    exec.schedule(() -> ScreenshotRecorder.saveScreenshot(
                        FabricLoader.getInstance().getGameDir().toFile(),
                        biome + ".png",
                        MinecraftClient.getInstance().getFramebuffer(),
                        (message) -> { }
                    ), 10, TimeUnit.SECONDS);
                    return 1;
                }
            )
        );
    }

    private static String randomizePosition(ServerPlayerEntity player) {
        ServerWorld world = player.getWorld();

        int x = rand.nextInt(10000000);
        int z = rand.nextInt(10000000);
        int yaw = rand.nextInt(360) - 180;
        int pitch = rand.nextInt(30) - 10;

        // Start force loading (Make data available before tp)
        world.setChunkForced(ChunkSectionPos.getSectionCoord(x), ChunkSectionPos.getSectionCoord(z), true);

        // Get top non-leaf block
        int y = world.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);

        // Get biome
        Optional<RegistryKey<Biome>> biomeRegistryKey = world.getBiome(new BlockPos(x, y, z)).getKey();

        // Stop force loading
        world.setChunkForced(ChunkSectionPos.getSectionCoord(x), ChunkSectionPos.getSectionCoord(z), false);

        if(biomeRegistryKey.isEmpty()) throw new IllegalStateException("BlockPos had no associated Biome");

        // Format biome
        String biomeID = biomeRegistryKey.get().getValue().getPath();
        System.out.println("Biome: " + biomeID);

        // TP
        player.teleport(world, x, y , z, yaw, pitch);
        return biomeID + "-" + x + "_" + z;
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

    private static void randomizeHotbar(ServerPlayerEntity player) {
        PlayerInventory inventory = player.getInventory();

        inventory.clear();

        // Fill individual hotbar slots
        for(int i = 0; i < 9; i++) {
            // Make some slots empty
            if(rand.nextInt(3)==0) continue;

            // Get random item
            Optional<RegistryEntry<Item>> randomItemOpt = Registry.ITEM.getRandom(rand);
            if(randomItemOpt.isEmpty()) throw new IllegalStateException("Random item was empty");
            Item randomItem = randomItemOpt.get().value();

            player.getInventory().insertStack(i, new ItemStack(randomItem, rand.nextBetween(1, randomItem.getMaxCount())));
        }

        int newSelectedSlot = rand.nextInt(9);
        player.getInventory().selectedSlot = newSelectedSlot;

        // Send Inventory update packet
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

    private static void randomizeHPandHunger(ServerPlayerEntity player) {
        HungerManager hungerManager = player.getHungerManager();

        player.setHealth(rand.nextBetween(1,20));
        hungerManager.setFoodLevel(rand.nextBetween(0,20));
        hungerManager.setSaturationLevel(rand.nextBetween(0,20));
    }
}
