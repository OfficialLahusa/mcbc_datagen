package com.lahusa.mcbc_datagen.mixin;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    @Inject(method = "canTarget(Lnet/minecraft/entity/LivingEntity;)Z", at = @At(value = "HEAD"), cancellable = true)
    public void makeInvisible(LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
        if(target instanceof PlayerEntity) {
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
