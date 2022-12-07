package com.lahusa.mcbc_datagen.mixin;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.level.ServerWorldProperties;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin {
    @Shadow @Final private ServerWorldProperties worldProperties;

    @Inject(method = "tickWeather", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/math/MathHelper;clamp(FFF)F", shift = At.Shift.BEFORE))
    private void injected(CallbackInfo ci) {
        ((ServerWorld)(Object)this).setRainGradient(worldProperties.isRaining() ? 1.0f : 0.0f);
    }
}
