package com.lahusa.mcbc_datagen.command;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;

public class ResolutionCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(
            ClientCommandManager.literal("resolution").executes(
                context -> {
                    MinecraftClient.getInstance().getWindow().setWindowedSize(640, 360);
                    return 1;
                }
            )
        );
    }
}
