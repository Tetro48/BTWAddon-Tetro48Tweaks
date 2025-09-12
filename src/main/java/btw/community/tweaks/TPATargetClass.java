package btw.community.tweaks;

import net.minecraft.src.*;

public class TPATargetClass {
	public Entity teleportingEntity;
	public Entity targetEntity;
	private final Vec3 initialPosOfTeleportingEntity;
	private final Vec3 initialPosOfTargetEntity;
	private boolean isTeleportingEntityInBounds;
	private boolean isTargetEntityInBounds;
	public long tickDuration;
	public int xpLevelCost;
	public TPATargetClass(Entity teleportingEntity, Entity targetEntity, long tickDuration, int xpLevelCost) {
		this.teleportingEntity = teleportingEntity;
		this.targetEntity = targetEntity;
		this.initialPosOfTeleportingEntity = Vec3.fakePool.getVecFromPool(teleportingEntity.posX, teleportingEntity.posY, teleportingEntity.posZ);
		this.initialPosOfTargetEntity = Vec3.fakePool.getVecFromPool(targetEntity.posX, targetEntity.posY, targetEntity.posZ);
		this.isTeleportingEntityInBounds = true;
		this.isTargetEntityInBounds = true;
		this.tickDuration = tickDuration;
		this.xpLevelCost = xpLevelCost;
	}

	public boolean withinBounds(Entity entity, Vec3 initialPos, double cube_size) {
		double x = entity.posX;
		double y = entity.posY;
		double z = entity.posZ;
		if (x > initialPos.xCoord - cube_size && x < initialPos.xCoord + cube_size
		 && y > initialPos.yCoord - cube_size && y < initialPos.yCoord + cube_size
		 && z > initialPos.zCoord - cube_size && z < initialPos.zCoord + cube_size) {
			return true;
		}
		return false;
	}

	public void update() {
		if (targetEntity == null && teleportingEntity instanceof EntityPlayerMP teleportingPlayer) {
			teleportingPlayer.sendChatToPlayer(ChatMessageComponent.createFromText("Your teleport target has either despawned, disconnected, or killed."));
		}
		if (teleportingEntity == null && targetEntity instanceof EntityPlayerMP targetPlayer) {
			targetPlayer.sendChatToPlayer(ChatMessageComponent.createFromText("That teleporting player has either despawned, disconnected, or killed."));
		}
		if (teleportingEntity == null || targetEntity == null) {
			return;
		}
		if (tickDuration == 80) {
			teleportingEntity.worldObj.playSoundEffect(teleportingEntity.posX, teleportingEntity.posY, teleportingEntity.posZ,"portal.trigger", 0.5f, 0.9f + 0.2f * teleportingEntity.rand.nextFloat());
		}
		boolean wasTeleportingEntityInBounds = this.isTeleportingEntityInBounds;
		boolean wasTargetEntityInBounds = this.isTargetEntityInBounds;
		this.isTeleportingEntityInBounds = withinBounds(teleportingEntity, initialPosOfTeleportingEntity, 8);
		this.isTargetEntityInBounds = withinBounds(targetEntity, initialPosOfTargetEntity, 8);
		if (this.isTeleportingEntityInBounds && this.isTargetEntityInBounds) {
			if (teleportingEntity instanceof EntityPlayerMP teleportingPlayer && teleportingPlayer.experienceLevel >= xpLevelCost) {
				tickDuration--;
			}
			else if (!(teleportingEntity instanceof EntityPlayerMP)) {
				tickDuration--;
			}
		}
		if ((wasTargetEntityInBounds && !this.isTargetEntityInBounds)
		   || (wasTeleportingEntityInBounds && !this.isTeleportingEntityInBounds)) {
			if (teleportingEntity instanceof EntityPlayerMP teleportingPlayer) {
				teleportingPlayer.sendChatToPlayer(ChatMessageComponent.createFromText("Someone got outside the teleport bounds, teleport is now paused."));
			}
			if (targetEntity instanceof EntityPlayerMP targetPlayer) {
				targetPlayer.sendChatToPlayer(ChatMessageComponent.createFromText("Someone got outside the teleport bounds, teleport is now paused."));
			}
		}
		if ((!wasTargetEntityInBounds && this.isTargetEntityInBounds)
				|| (!wasTeleportingEntityInBounds && this.isTeleportingEntityInBounds)) {
			if (teleportingEntity instanceof EntityPlayerMP teleportingPlayer) {
				teleportingPlayer.sendChatToPlayer(ChatMessageComponent.createFromText("Someone got inside the teleport bounds."));
			}
			if (targetEntity instanceof EntityPlayerMP targetPlayer) {
				targetPlayer.sendChatToPlayer(ChatMessageComponent.createFromText("Someone got inside the teleport bounds."));
			}
		}

		if (tickDuration <= 0) {
			teleportingEntity.copyLocationAndAnglesFrom(targetEntity);
			if (teleportingEntity instanceof EntityPlayerMP teleportingPlayer) {
				teleportingPlayer.setPositionAndUpdate(targetEntity.posX, targetEntity.posY, targetEntity.posZ);
				teleportingPlayer.experienceLevel -= xpLevelCost;
				teleportingPlayer.playerNetServerHandler.sendPacketToPlayer(new Packet43Experience(teleportingPlayer.experience, teleportingPlayer.experienceTotal, teleportingPlayer.experienceLevel));
			}
			teleportingEntity.playSound("mob.endermen.portal", 0.5f, 0.75f);
		}
	}
}
