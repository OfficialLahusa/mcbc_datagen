package com.lahusa.mcbc_datagen;

import com.lahusa.mcbc_datagen.command.DataGenCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class MCBCDataGenMod implements ModInitializer {

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
