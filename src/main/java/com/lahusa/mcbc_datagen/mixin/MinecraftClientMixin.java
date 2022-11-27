package com.lahusa.mcbc_datagen.mixin;

import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "onWindowFocusChanged", at = @At(value = "HEAD"), cancellable = true)
    public void alwaysFocus(boolean focused, CallbackInfo ci) {
        ((MinecraftClientAccessor)(Object)this).setWindowFocused(true);
        ci.cancel();
    }
}
