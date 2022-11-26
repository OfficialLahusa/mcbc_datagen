package com.lahusa.mcbc_datagen;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.entity.player.PlayerInventory;

@net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
public class MCBCDataGenClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // Register network packet handlers
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
