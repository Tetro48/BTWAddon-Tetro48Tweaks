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
		return "/tpa <send/accept/cost/decline> <player> OR /tpa abort";
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender par1ICommandSender) {
		return true;
	}

	@Override
	public List getCommandAliases() {
		return List.of("tpask", "/teleportask", "tprequest", "teleportrequest");
	}

	@Override
	public List addTabCompletionOptions(ICommandSender par1ICommandSender, String[] strings) {
		if (strings.length == 1) {
			return getListOfStringsMatchingLastWord(strings, "send", "accept", "cost", "decline", "abort");
		} else {
			return strings.length == 2 ? getListOfStringsMatchingLastWord(strings, this.getListOfPlayerUsernames()) : null;
		}
	}

	private String[] getListOfPlayerUsernames() {
		return MinecraftServer.getServer().getAllUsernames();
	}

	@Override
	public void processCommand(ICommandSender iCommandSender, String[] strings) {
		if (strings.length >= 1 && strings[0].equals("abort")) {
			for (TPATargetClass tpaTarget : TPATargets) {
				if (tpaTarget.teleportingEntity.equals(getCommandSenderAsPlayer(iCommandSender))) {
					iCommandSender.sendChatToPlayer(ChatMessageComponent.createFromText("Aborted teleporting."));
					((EntityPlayerMP) tpaTarget.targetEntity).sendChatToPlayer(ChatMessageComponent.createFromText(String.format("%s has aborted teleporting.", ((EntityPlayerMP) tpaTarget.teleportingEntity).getCommandSenderName())));
					TPARequests.remove(tpaTarget);
					TPATargets.remove(tpaTarget);
					return;
				}
			}
			throw new CommandException("There's no teleports to abort.");
		}
		if (strings.length < 2) throw new WrongUsageException(getCommandUsage(iCommandSender));
		else if (strings[1].equals(iCommandSender.getCommandSenderName())) {
			throw new CommandException("You can't send a TP request to yourself.");
		}
		else if (strings[0].equals("cost")) {
			for (Object player : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
				if (player instanceof EntityPlayerMP targetPlayer) {
					if (targetPlayer.getEntityName().equals(strings[1])) {
						EntityPlayerMP teleportingPlayer = getCommandSenderAsPlayer(iCommandSender);
						if (targetPlayer.dimension != teleportingPlayer.dimension) {
							throw new CommandException("Your target is outside your dimension.");
						}
						int xpCost = getXpCost(targetPlayer, teleportingPlayer);
						iCommandSender.sendChatToPlayer(ChatMessageComponent.createFromText("The XP level cost to teleport is " + xpCost));
						return;
					}
				}
			}
		}
		else if (strings[0].equals("accept") || strings[0].equals("decline")) {
			for (TPATargetClass tpaRequest : TPARequests) {
				if (tpaRequest.targetEntity == getCommandSenderAsPlayer(iCommandSender) && ((EntityPlayerMP) tpaRequest.teleportingEntity).getCommandSenderName().equals(strings[1])) {
					if (strings[0].equals("accept")) {
						if (((EntityPlayerMP) tpaRequest.teleportingEntity).experienceLevel < getXpCost(tpaRequest.targetEntity, tpaRequest.teleportingEntity)) {
							iCommandSender.sendChatToPlayer(ChatMessageComponent.createFromText(String.format("The requested teleport got aborted due to %s not having enough XP levels.", ((EntityPlayerMP) tpaRequest.teleportingEntity).getCommandSenderName())));
							((EntityPlayerMP) tpaRequest.teleportingEntity).sendChatToPlayer(ChatMessageComponent.createFromText(String.format("Your teleport request to %s failed due to you not having enough XP levels.", ((EntityPlayerMP) tpaRequest.targetEntity).getCommandSenderName())));
							return;
						}
						tpaRequest.tickDuration = 100;
						tpaRequest.xpLevelCost = getXpCost(tpaRequest.targetEntity, tpaRequest.teleportingEntity);
						TPATargets.add(tpaRequest);
						iCommandSender.sendChatToPlayer(ChatMessageComponent.createFromText(String.format("Accepted the TP request from %s, and will be teleported to you in %.2f seconds.", ((EntityPlayerMP) tpaRequest.teleportingEntity).getCommandSenderName(), tpaRequest.tickDuration / 20d)));
						((EntityPlayerMP) tpaRequest.teleportingEntity).sendChatToPlayer(ChatMessageComponent.createFromText(String.format("Your teleport request to %s is accepted, and you are going to be teleported in %.2f seconds.", ((EntityPlayerMP) tpaRequest.targetEntity).getCommandSenderName(), tpaRequest.tickDuration / 20d)));
					} else {
						iCommandSender.sendChatToPlayer(ChatMessageComponent.createFromText(String.format("Declined the TP request from %s.", ((EntityPlayerMP) tpaRequest.teleportingEntity).getCommandSenderName())));
						((EntityPlayerMP) tpaRequest.teleportingEntity).sendChatToPlayer(ChatMessageComponent.createFromText(String.format("Your teleport request to %s is declined.", ((EntityPlayerMP) tpaRequest.targetEntity).getCommandSenderName())));
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
						int xpCost = getXpCost(targetPlayer, teleportingPlayer);

						if (targetPlayer.dimension != teleportingPlayer.dimension) {
							throw new CommandException("Your target is outside your dimension.");
						}

						if (teleportingPlayer.experienceLevel < xpCost) {
							throw new CommandException("Your XP level is too low for the teleport distance.");
						}

						TPARequests.add(new TPATargetClass(teleportingPlayer, targetPlayer, 0, xpCost));
						iCommandSender.sendChatToPlayer(ChatMessageComponent.createFromText("You've sent a TP request to " + targetPlayer.getCommandSenderName() + ". This'll cost you " + xpCost + " XP levels on teleport."));
						targetPlayer.sendChatToPlayer(ChatMessageComponent.createFromText("You've been sent a TP request from " + iCommandSender.getCommandSenderName() + ". This'll cost them some XP levels on teleport."));
						return;
					}
				}
			}
			throw new WrongUsageException(getCommandUsage(iCommandSender));
		}
	}

	private static int getXpCost(Entity targetEntity, Entity teleportingEntity) {
		double distX = Math.abs(targetEntity.posX - teleportingEntity.posX);
		double distZ = Math.abs(targetEntity.posZ - teleportingEntity.posZ);
		int xpCost = 0;
		xpCost += (int) (Math.sqrt((distX * distX) + (distZ * distZ)) / 300);
		xpCost += (int) (Math.max(0, 64 - teleportingEntity.posY) / 1.5);
		return xpCost;
	}
}
