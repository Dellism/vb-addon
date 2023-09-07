package me.vbaddon.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import me.vbaddon.modules.BetterTabPlus;
import net.minecraft.client.gui.hud.InGameHud;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import meteordevelopment.meteorclient.systems.modules.Modules;

@Mixin(InGameHud.class)
public class InGameHudMixin {
    @ModifyExpressionValue(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/option/KeyBinding;isPressed()Z"))
    private boolean alwaysPress(boolean original) {
        BetterTabPlus betterTabPlus = Modules.get().get(BetterTabPlus.class);
        return betterTabPlus.isActive() ? betterTabPlus.alwaysShow.get() || original : original;
    }
}
