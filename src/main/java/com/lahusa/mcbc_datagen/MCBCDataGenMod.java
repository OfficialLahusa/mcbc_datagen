package com.lahusa.mcbc_datagen;

import com.lahusa.mcbc_datagen.command.DataGenCommand;
import com.lahusa.mcbc_datagen.util.DataGenerationManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.util.Identifier;

public class MCBCDataGenMod implements ModInitializer {

    public static final String MODID = "mcbc_datagen";
    public static final Identifier INVENTORY_SLOT_CHANGE_PACKET_ID = new Identifier(MODID, "inv_slot_change");
    public static final Identifier SET_HUD_HIDDEN_PACKET_ID = new Identifier(MODID, "set_hud_hidden");
    public static final Identifier FORCE_SCREENSHOT_PACKET_ID = new Identifier(MODID, "force_screenshot");
    public static final Identifier SCREENSHOT_CONFIRMATION_PACKET_ID = new Identifier(MODID, "screenshot_confirmation");
    public static final Identifier FORCE_RESOLUTION_CHANGE_PACKET_ID = new Identifier(MODID, "force_resolution_change");

    @Override
    public void onInitialize() {
        System.out.println("MCBC DataGen Init: Starting");

        // Register Commands
        CommandRegistrationCallback.EVENT.register(
            (dispatcher, registryAccess, environment) -> DataGenCommand.register(dispatcher)
        );

        // Remove disconnected clients from data generation schedules
        ServerPlayConnectionEvents.DISCONNECT.register(
                (handler, server) -> DataGenerationManager.removePlayer(handler.getPlayer())
        );

        // Server tick events
        ServerLifecycleEvents.SERVER_STARTED.register(
                server -> DataGenerationManager.initialize(server.getPlayerManager())
        );
        ServerLifecycleEvents.SERVER_STOPPED.register(
                server -> DataGenerationManager.clear()
        );
        ServerTickEvents.END_SERVER_TICK.register(
                server -> DataGenerationManager.tick()
        );

        // Server packet handlers
        ServerPlayNetworking.registerGlobalReceiver(SCREENSHOT_CONFIRMATION_PACKET_ID,
                (server, player, handler, buf, responseSender) -> DataGenerationManager.handleScreenShotConfirmation(player)
        );

        System.out.println("MCBC DataGen Init: Done");
    }
}
