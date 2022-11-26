package com.lahusa.mcbc_datagen;

import com.lahusa.mcbc_datagen.command.DataGenCommand;
import com.lahusa.mcbc_datagen.command.ResolutionCommand;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerInventory;

import java.util.Objects;

@net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
public class MCBCDataGenClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Clientside commands
        ClientCommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess) -> ResolutionCommand.register(dispatcher)
        );

        // Clientside network packet handlers
        // Selected slot update packet
        ClientPlayNetworking.registerGlobalReceiver(
            MCBCDataGenMod.INVENTORY_SLOT_CHANGE_PACKET_ID,
            (client, handler, buf, responseSender) -> {
                if (client.player != null) {
                    PlayerInventory inventory = client.player.getInventory();
                    inventory.selectedSlot = buf.readInt();
                }
            }
        );
    }
}
