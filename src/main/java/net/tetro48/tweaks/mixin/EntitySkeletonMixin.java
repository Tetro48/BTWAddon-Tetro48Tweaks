package net.tetro48.tweaks.mixin;

import btw.entity.mob.behavior.SkeletonArrowAttackBehavior;
import net.minecraft.src.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntitySkeleton.class)
public abstract class EntitySkeletonMixin extends EntityMob {
	@Shadow private EntityAIAttackOnCollide aiMeleeAttack;
	@Shadow private SkeletonArrowAttackBehavior aiRangedAttack;

	@Unique private boolean isTargetEntityInRange = false;
	@Unique private boolean wasTargetEntityInRange = false;
	@Unique private boolean hasSkeletonShot = false;
	@Unique private boolean isSkeletonMelee = false;

	//this is in ticks
	@Unique private int switchBackGracePeriod = 0;

	public EntitySkeletonMixin(World par1World) {
		super(par1World);
	}

	@Inject(method = "attackEntityWithRangedAttack", at = @At("HEAD"))
	private void markRangedShot(EntityLivingBase target, float fDamageModifier, CallbackInfo ci) {
		hasSkeletonShot = true;
	}

	@Inject(method = "onLivingUpdate", at = @At("RETURN"))
	private void meleeWhenUpClose(CallbackInfo ci) {
		EntityLivingBase target = getAttackTarget();
		if (target != null) {
			isTargetEntityInRange = getAttackTarget().getDistanceToEntity(this) < 1.25f;
		}
		if (switchBackGracePeriod > 0) {
			switchBackGracePeriod--;
		}
		if (!isTargetEntityInRange) {
			hasSkeletonShot = false;
		}
		if (isTargetEntityInRange && !isSkeletonMelee && hasSkeletonShot) {
			this.tasks.removeTask(this.aiRangedAttack);
			this.tasks.addTask(3, this.aiMeleeAttack);
			isSkeletonMelee = true;
			hasSkeletonShot = false;
			switchBackGracePeriod = 60;
		}
		else if (!isTargetEntityInRange && isSkeletonMelee && switchBackGracePeriod <= 0) {
			ItemStack heldStack = this.getHeldItem();
			if (heldStack != null && heldStack.itemID == Item.bow.itemID) {
				this.tasks.removeTask(this.aiMeleeAttack);
				this.tasks.addTask(4, this.aiRangedAttack);
				isSkeletonMelee = false;
			}
		}
		wasTargetEntityInRange = isTargetEntityInRange;
	}
}
