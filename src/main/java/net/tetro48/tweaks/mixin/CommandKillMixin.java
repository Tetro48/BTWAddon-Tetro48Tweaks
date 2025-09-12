package net.tetro48.tweaks.mixin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.src.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandKill.class)
public abstract class CommandKillMixin {
	@Inject(method = "processCommand", at = @At("HEAD"), cancellable = true)
	private void addKillOtherEntitiesAbility(ICommandSender iCommandSender, String[] strings, CallbackInfo ci) {
		if (strings.length > 0) {
			if (strings[0].equals("@a")) {
				for (Object player : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
					if (player instanceof EntityPlayerMP entityPlayerMP) {
						entityPlayerMP.attackEntityFrom(DamageSource.outOfWorld, Float.MAX_VALUE);
						iCommandSender.sendChatToPlayer(ChatMessageComponent.createFromText("Killed all players."));
					}
				}
			} else {
				for (Object player : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
					if (player instanceof EntityPlayerMP entityPlayerMP) {
						if (strings[0].equals(entityPlayerMP.getEntityName())) {
							entityPlayerMP.attackEntityFrom(DamageSource.outOfWorld, Float.MAX_VALUE);
							iCommandSender.sendChatToPlayer(ChatMessageComponent.createFromTranslationKey("Killed " + strings[0] + "."));
						}
					}
				}
			}
			ci.cancel();
		}
	}
}
