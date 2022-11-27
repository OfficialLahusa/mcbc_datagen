package com.lahusa.mcbc_datagen;

import com.lahusa.mcbc_datagen.command.ResolutionCommand;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.entity.player.PlayerInventory;

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
        // Set hud hidden packet
        ClientPlayNetworking.registerGlobalReceiver(
                MCBCDataGenMod.SET_HUD_HIDDEN_PACKET_ID,
                (client, handler, buf, responseSender) -> {
                    client.options.hudHidden = buf.readBoolean();
                }
        );
        // Force screenshot packet
        ClientPlayNetworking.registerGlobalReceiver(
                MCBCDataGenMod.FORCE_SCREENSHOT_PACKET_ID,
                (client, handler, buf, responseSender) -> {
                    String filename = buf.readString();
                    ScreenshotRecorder.saveScreenshot(
                            FabricLoader.getInstance().getGameDir().toFile(),
                            filename,
                            client.getFramebuffer(),
                            (message) -> { }
                    );

                    // Send confirmation to server
                    ClientPlayNetworking.send(MCBCDataGenMod.SCREENSHOT_CONFIRMATION_PACKET_ID, PacketByteBufs.create());
                }
        );
    }
}
