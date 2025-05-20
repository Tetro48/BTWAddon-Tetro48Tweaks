package net.tetro48.tweaks.mixin;

import btw.entity.mob.behavior.SkeletonArrowAttackBehavior;
import net.minecraft.src.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntitySkeleton.class)
public abstract class EntitySkeletonMixin extends EntityMob {
	@Shadow private EntityAIAttackOnCollide aiMeleeAttack;
	@Shadow private SkeletonArrowAttackBehavior aiRangedAttack;

	@Shadow public abstract void setCombatTask();

	@Unique private boolean isTargetEntityInRange = false;
	@Unique private boolean wasTargetEntityInRange = false;

	public EntitySkeletonMixin(World par1World) {
		super(par1World);
	}

	@Inject(method = "onLivingUpdate", at = @At("RETURN"))
	private void meleeWhenUpClose(CallbackInfo ci) {
		if (getAttackTarget() != null) {
			isTargetEntityInRange = getAttackTarget().getDistanceToEntity(this) < 1.25f;
		}
		if (isTargetEntityInRange && !wasTargetEntityInRange) {
			this.tasks.removeTask(this.aiMeleeAttack);
			this.tasks.removeTask(this.aiRangedAttack);
			this.tasks.addTask(4, this.aiMeleeAttack);
		}
		else if (!isTargetEntityInRange) {
			ItemStack heldStack = this.getHeldItem();
			if (heldStack != null && heldStack.itemID == Item.bow.itemID) {
				this.tasks.removeTask(this.aiMeleeAttack);
				this.tasks.addTask(4, this.aiRangedAttack);
			}
		}
		wasTargetEntityInRange = isTargetEntityInRange;
	}
}
