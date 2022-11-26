package com.lahusa.mcbc_datagen;

import com.lahusa.mcbc_datagen.command.DataGenCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.util.Identifier;

public class MCBCDataGenMod implements ModInitializer {

    public static final String MODID = "mcbc_datagen";

    public static final Identifier INVENTORY_SLOT_CHANGE_PACKET_ID = new Identifier(MODID, "inv_slot_change");

    @Override
    public void onInitialize() {
        System.out.println("MCBC DataGen Init: Starting");

        // Register Commands
        CommandRegistrationCallback.EVENT.register(
            (dispatcher, registryAccess, environment) -> DataGenCommand.register(dispatcher)
        );

        System.out.println("MCBC DataGen Init: Done");
    }
}
