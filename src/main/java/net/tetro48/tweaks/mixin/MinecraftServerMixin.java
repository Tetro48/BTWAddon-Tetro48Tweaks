package net.tetro48.tweaks.mixin;

import btw.community.tweaks.TweaksAddon;
import net.minecraft.server.MinecraftServer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
	@Inject(method = "tick", at = @At("RETURN"))
	public void tick(CallbackInfo ci) {
		TweaksAddon.updateTPATargets();
	}
}
