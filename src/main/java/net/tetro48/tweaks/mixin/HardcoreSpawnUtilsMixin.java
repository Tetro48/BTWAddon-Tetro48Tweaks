package net.tetro48.tweaks.mixin;

import btw.util.hardcorespawn.HardcoreSpawnUtils;
import net.minecraft.server.MinecraftServer;
import net.minecraft.src.EntityPlayerMP;
import net.minecraft.src.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HardcoreSpawnUtils.class)
public abstract class HardcoreSpawnUtilsMixin {
	@Shadow
	private static void countDeath(EntityPlayerMP oldPlayer, EntityPlayerMP newPlayer) {
	}

	@Inject(method = "handleHardcoreSpawn", at = @At(value = "INVOKE", target = "Lbtw/util/hardcorespawn/HardcoreSpawnUtils;returnPlayerToOriginalSpawn(Lnet/minecraft/src/World;Lnet/minecraft/src/EntityPlayerMP;)V"))
	private static void makeDeathCountOnClassic(MinecraftServer server, EntityPlayerMP oldPlayer, EntityPlayerMP newPlayer, CallbackInfo ci) {
		if (!oldPlayer.playerConqueredTheEnd) {
			countDeath(oldPlayer, newPlayer);
		}
	}
}
