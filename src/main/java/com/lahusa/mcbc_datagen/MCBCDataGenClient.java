package com.lahusa.mcbc_datagen;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.ScreenshotRecorder;
import net.minecraft.entity.player.PlayerInventory;
import org.lwjgl.glfw.GLFW;

@net.fabricmc.api.Environment(net.fabricmc.api.EnvType.CLIENT)
public class MCBCDataGenClient implements ClientModInitializer {

    private static KeyBinding unlockKey;

    @Override
    public void onInitializeClient() {
        // Keybind registration
        unlockKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.mcbc_datagen.unlock", // The translation key of the keybinding's name
                InputUtil.Type.KEYSYM, // The type of the keybinding, KEYSYM for keyboard, MOUSE for mouse.
                GLFW.GLFW_KEY_PAGE_UP, // The keycode of the key
                "category.mcbc_datagen.keybinds" // The translation key of the keybinding's category.
        ));

        // Keybind handlers
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (unlockKey.wasPressed()) {
                client.mouse.unlockCursor();
            }
        });

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
                (client, handler, buf, responseSender) -> client.options.hudHidden = buf.readBoolean()
        );
        // Force resolution packet
        ClientPlayNetworking.registerGlobalReceiver(
                MCBCDataGenMod.FORCE_RESOLUTION_CHANGE_PACKET_ID,
                (client, handler, buf, responseSender) -> client.getWindow().setWindowedSize(640, 360)
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
