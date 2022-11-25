package com.lahusa.mcbc_datagen.mixin;

import net.minecraft.client.toast.AdvancementToast;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(AdvancementToast.class)
public class AdvancementToastMixin {

    @Redirect(method = "draw", at = @At(value = "HEAD"))
    public Toast.Visibility hideAllAdvancementToasts(MatrixStack matrices, ToastManager manager, long startTime) {
        return Toast.Visibility.HIDE;
    }
}
