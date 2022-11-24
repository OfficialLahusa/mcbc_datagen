package com.lahusa.mcbc_datagen;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.io.*;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static net.minecraft.server.command.CommandManager.*;

public class MCBCDataGenMod implements ModInitializer {

    @Override
    public void onInitialize() {
        System.out.println("MCBC DataGen Init: Starting");
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("train")
                .executes(context -> {
                    // randomize hotbar
                    randomHotbar(context);

                    //randomize Time and Weather
                    randomTimeWeather(context);

                    // teleport and get biome
                    String biome = randomTeleport(context);

                    // wait
                    ScheduledThreadPoolExecutor exec = new ScheduledThreadPoolExecutor(1);
                    exec.schedule(new Runnable() {
                        public void run() {
                            ScreenshotRecorder.saveScreenshot(new File("D:\\Screenshots"), biome + ".png", MinecraftClient.getInstance().getFramebuffer(), (message) -> {
                            });
                        }
                    }, 10, TimeUnit.SECONDS);

                    // sendMessage(Text.literal("Called /foo with no arguments"));
                    return 1;
                })));
        System.out.println("MCBC DataGen Init: Done");
    }

    public String randomTeleport(CommandContext<ServerCommandSource> context) {
        int count = 0;
        while(true) {
            count ++;
            ServerPlayerEntity player = context.getSource().getPlayer();
            Random rand = new Random();
            int x = rand.nextInt(10000000);
            int y;
            int z = rand.nextInt(10000000);
            int yaw = rand.nextInt(360) - 180;
            int pitch = rand.nextInt(30) - 10;
            for (y = 320; y > 0; y--) {
                BlockState block = player.getWorld().getBlockState(new BlockPos(x, y, z));
                // block.getMaterial();
                if (block.getMaterial().isSolid()) { //.equals(Material.AIR)
                    break;
                }
            }
            String biomeData = player.getWorld().getBiome(new BlockPos(x, y, z)).getKey().get().toString();
            String biome = biomeData.substring(biomeData.indexOf("/ minecraft:") + 12, biomeData.indexOf("]"));
            System.out.println(Integer.toString(count) + ". Try, Biome: " + biome);

            if (!player.getWorld().getBlockState(new BlockPos(x, y + 1, z)).getMaterial().isLiquid()) {
                player.teleport(player.getWorld(), x, y + 1, z, yaw, pitch);
                return biome;
            }
        }
    }

    public void randomHotbar(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        player.getInventory().clear();
        Random rand = new Random();

        for(int i = 0; i < 9; i++) {
            player.getInventory().insertStack(i, new ItemStack(Item.byRawId(rand.nextInt(2267)), rand.nextInt(64)));
        }
    }

    public void randomTimeWeather(CommandContext<ServerCommandSource> context) {
        ServerPlayerEntity player = context.getSource().getPlayer();
        Random rand = new Random();

        player.getWorld().setWeather(0, 0, rand.nextBoolean(), rand.nextBoolean());
        player.getWorld().setTimeOfDay(rand.nextLong(2147483647));
    }
}
