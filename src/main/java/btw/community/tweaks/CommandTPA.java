package btw.community.tweaks;

import net.minecraft.server.MinecraftServer;
import net.minecraft.src.*;

import java.util.List;

import static btw.community.tweaks.TweaksAddon.TPARequests;
import static btw.community.tweaks.TweaksAddon.TPATargets;

public class CommandTPA extends CommandBase {
	@Override
	public String getCommandName() {
		return "tpa";
	}

	@Override
	public String getCommandUsage(ICommandSender iCommandSender) {
		return "/tpa [send/accept/decline] <player> OR /tpa abort";
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender par1ICommandSender) {
		return true;
	}

	@Override
	public List getCommandAliases() {
		return List.of("tpask", "/teleportask");
	}

	@Override
	public List addTabCompletionOptions(ICommandSender par1ICommandSender, String[] strings) {
		if (strings.length == 1) {
			return getListOfStringsMatchingLastWord(strings, "send", "accept", "decline");
		} else {
			return strings.length == 2 ? getListOfStringsMatchingLastWord(strings, this.getListOfPlayerUsernames()) : null;
		}
	}

	private String[] getListOfPlayerUsernames() {
		return MinecraftServer.getServer().getAllUsernames();
	}

	@Override
	public void processCommand(ICommandSender iCommandSender, String[] strings) {
		if (strings[0].equals("abort")) {
			for (TPATargetClass tpaTarget : TPATargets) {
				if (tpaTarget.teleportingEntity == getCommandSenderAsPlayer(iCommandSender)) {
					iCommandSender.sendChatToPlayer(ChatMessageComponent.createFromText("Aborted teleporting."));
					((EntityPlayerMP) tpaTarget.targetEntity).sendChatToPlayer(ChatMessageComponent.createFromText(String.format("%s has aborted teleporting.", ((EntityPlayerMP) tpaTarget.teleportingEntity).getCommandSenderName())));
					TPARequests.remove(tpaTarget);
					return;
				}
			}
			throw new CommandException("There's no teleports to abort.");
		}
		if (strings.length < 2) throw new WrongUsageException(getCommandUsage(iCommandSender));
		if (strings[1].equals(iCommandSender.getCommandSenderName()))
			throw new CommandException("You can't send a TP request to yourself.");
		if (strings[0].equals("accept") || strings[0].equals("decline")) {
			for (TPATargetClass tpaRequest : TPARequests) {
				if (tpaRequest.targetEntity == getCommandSenderAsPlayer(iCommandSender) && ((EntityPlayerMP) tpaRequest.teleportingEntity).getCommandSenderName().equals(strings[1])) {
					if (strings[0].equals("accept")) {
						double distX = Math.abs(tpaRequest.targetEntity.posX - tpaRequest.teleportingEntity.posX);
						double distZ = Math.abs(tpaRequest.targetEntity.posZ - tpaRequest.teleportingEntity.posZ);
						long ticks = 0;
						ticks += (long) (Math.sqrt((distX * distX) + (distZ * distZ)) * 10);
						ticks += (long) (Math.max(0, 64 - tpaRequest.teleportingEntity.posY) * 200);
						tpaRequest.tickDuration = ticks;
						TPATargets.add(tpaRequest);
						iCommandSender.sendChatToPlayer(ChatMessageComponent.createFromText(String.format("Accepted the TP request from %s, and will be teleported to you in %.2f seconds.", ((EntityPlayerMP) tpaRequest.teleportingEntity).getCommandSenderName(), tpaRequest.tickDuration / 20d)));
						((EntityPlayerMP) tpaRequest.teleportingEntity).sendChatToPlayer(ChatMessageComponent.createFromText(String.format("Your teleport request to %s is accepted, and you are going to be teleported in %.2f seconds.", ((EntityPlayerMP) tpaRequest.teleportingEntity).getCommandSenderName(), tpaRequest.tickDuration / 20d)));
					} else {
						iCommandSender.sendChatToPlayer(ChatMessageComponent.createFromText(String.format("Declined the TP request from %s.", ((EntityPlayerMP) tpaRequest.teleportingEntity).getCommandSenderName())));
						((EntityPlayerMP) tpaRequest.teleportingEntity).sendChatToPlayer(ChatMessageComponent.createFromText(String.format("Your teleport request to %s is declined.", ((EntityPlayerMP) tpaRequest.teleportingEntity).getCommandSenderName())));
					}
					TPARequests.remove(tpaRequest);
					return;
				}
			}
			throw new CommandException("No correct request was found.");
		} else if (strings[0].equals("send")) {
			for (TPATargetClass tpaTarget : TPATargets) {
				if (tpaTarget.teleportingEntity == getCommandSenderAsPlayer(iCommandSender)) {
					throw new CommandException("You're being teleported.");
				}
			}
			for (TPATargetClass tpaRequest : TPARequests) {
				if (((EntityPlayerMP) tpaRequest.targetEntity).getCommandSenderName().equals(strings[1])
						&& tpaRequest.teleportingEntity == getCommandSenderAsPlayer(iCommandSender)) {
					throw new CommandException("You've already sent that teleport request.");
				}
			}
			for (Object player : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
				if (player instanceof EntityPlayerMP targetPlayer) {
					if (targetPlayer.getEntityName().equals(strings[1])) {
						EntityPlayerMP teleportingPlayer = getCommandSenderAsPlayer(iCommandSender);
						if (targetPlayer.dimension != teleportingPlayer.dimension) {
							throw new CommandException("Your target is outside your dimension.");
						}
						TPARequests.add(new TPATargetClass(teleportingPlayer, targetPlayer, 0));
						iCommandSender.sendChatToPlayer(ChatMessageComponent.createFromText("You've sent a TP request to " + targetPlayer.getCommandSenderName()));
						targetPlayer.sendChatToPlayer(ChatMessageComponent.createFromText("You've been sent a TP request from " + iCommandSender.getCommandSenderName()));
						return;
					}
				}
			}
			throw new WrongUsageException(getCommandUsage(iCommandSender));
		}
	}
}
