package com.lahusa.mcbc_datagen.command;

import com.lahusa.mcbc_datagen.util.DataGenerationManager;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class DataGenCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
            literal("datagen")
                .requires(source -> source.hasPermissionLevel(2))
                .then(argument("iterations", IntegerArgumentType.integer(1))
                    .executes(
                        context -> {
                            ServerPlayerEntity player = context.getSource().getPlayer();
                            if(player == null) {
                                throw new SimpleCommandExceptionType(Text.literal("This command has to be executed by a player")).create();
                            }

                            DataGenerationManager.schedule(player, IntegerArgumentType.getInteger(context, "iterations"));
                            return 1;
                        }
                ))
                .executes(
                    context -> {
                        throw new SimpleCommandExceptionType(Text.literal("Usage: /datagen <iterations>")).create();
                    }
                )
        );
    }
}
