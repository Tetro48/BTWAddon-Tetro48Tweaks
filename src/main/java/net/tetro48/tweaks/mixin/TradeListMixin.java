package net.tetro48.tweaks.mixin;

import btw.entity.mob.villager.trade.TradeList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(TradeList.class)
public abstract class TradeListMixin {
	@ModifyArgs(method = "addBlacksmithTrades", remap = false, at = @At(value = "INVOKE", target = "Lbtw/entity/mob/villager/trade/TradeProvider$SecondaryCostStep;secondaryEmeraldCost(II)Lbtw/entity/mob/villager/trade/TradeProvider$FinalStep;"))
	private static void reduceBlacksmithScrollCost(Args args) {
		args.set(0, 32);
		args.set(0, 48);
	}
	@ModifyArgs(method = "addFarmerTrades", remap = false, at = @At(value = "INVOKE", target = "Lbtw/entity/mob/villager/trade/TradeProvider$SecondaryCostStep;secondaryEmeraldCost(II)Lbtw/entity/mob/villager/trade/TradeProvider$FinalStep;"))
	private static void reduceFarmerScrollCost(Args args) {
		args.set(0, 32);
		args.set(0, 48);
	}
	@ModifyArgs(method = "addLibrarianTrades", remap = false, at = @At(value = "INVOKE", target = "Lbtw/entity/mob/villager/trade/TradeProvider$SecondaryCostStep;secondaryEmeraldCost(II)Lbtw/entity/mob/villager/trade/TradeProvider$FinalStep;"))
	private static void reduceLibrarianScrollCost(Args args) {
		args.set(0, 32);
		args.set(0, 48);
	}
	@ModifyArgs(method = "addPriestTrades", remap = false, at = @At(value = "INVOKE", ordinal = 16, target = "Lbtw/entity/mob/villager/trade/TradeProvider$SecondaryCostStep;secondaryEmeraldCost(II)Lbtw/entity/mob/villager/trade/TradeProvider$FinalStep;"))
	private static void reducePriestScrollCost(Args args) {
		args.set(0, 32);
		args.set(0, 48);
	}
	@ModifyArgs(method = "addButcherTrades", remap = false, at = @At(value = "INVOKE", ordinal = 1, target = "Lbtw/entity/mob/villager/trade/TradeProvider$SecondaryCostStep;secondaryEmeraldCost(II)Lbtw/entity/mob/villager/trade/TradeProvider$FinalStep;"))
	private static void reduceButcherScrollCost(Args args) {
		args.set(0, 32);
		args.set(0, 48);
	}
}
