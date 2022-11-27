package com.lahusa.mcbc_datagen.mixin;

import com.lahusa.mcbc_datagen.util.DataGenerationManager;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    @Inject(method = "onTeleportConfirm", at = @At("TAIL"))
    public void updateScheduleOnTeleport(CallbackInfo ci) {
        DataGenerationManager.handleTeleportConfirmation(((ServerPlayNetworkHandler)(Object)this).player);
    }
}
