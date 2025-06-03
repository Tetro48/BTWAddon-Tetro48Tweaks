package net.tetro48.tweaks.mixin;

import btw.community.tweaks.TweaksAddon;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldServer.class)
public abstract class WorldServerMixin extends World {
	@Shadow @Final private MinecraftServer mcServer;

	public WorldServerMixin(ISaveHandler par1ISaveHandler, String par2Str, WorldProvider par3WorldProvider, WorldSettings par4WorldSettings, Profiler par5Profiler, ILogAgent par6ILogAgent) {
		super(par1ISaveHandler, par2Str, par3WorldProvider, par4WorldSettings, par5Profiler, par6ILogAgent);
	}
	@Unique private boolean isTimePaused = true;

	@Inject(method = "tick", at = @At("HEAD"), cancellable = true)
	private void dontTickTimeWhenNoPlayers(CallbackInfo ci) {
		if (TweaksAddon.enableTimeFreeze) {
			if (mcServer.getCurrentPlayerCount() <= 0) {
				if (!isTimePaused && this.provider.dimensionId == 0) {
					mcServer.logInfo("The server has now paused ticking worlds");
				}
				isTimePaused = true;
				ci.cancel();
			} else {
				if (isTimePaused && this.provider.dimensionId == 0) {
					mcServer.logInfo("The server has now resumed ticking worlds");
				}
				isTimePaused = false;
			}
		}
	}
}
