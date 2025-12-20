package btw.community.tweaks;

import api.AddonHandler;
import api.BTWAddon;
import api.config.AddonConfig;
import btw.block.BTWBlocks;
import btw.crafting.recipe.RecipeManager;
import btw.item.BTWItems;
import net.minecraft.src.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class TweaksAddon extends BTWAddon {
	private static TweaksAddon instance;

	public static boolean enableTPA = false;
	public static boolean enableTimeFreeze = true;

	public static List<TPATargetClass> TPATargets;
	public static List<TPATargetClass> TPARequests;

	public TweaksAddon() {
		super();
	}

	@Override
	public void registerConfigProperties(AddonConfig config) {
		config.registerBoolean("enable-tpa", false, "This toggles the /tpa command.");
		config.updatePath("EnableTPA", "enable-tpa");
		config.registerBoolean("enable-time-freeze", true, "This toggles the time freeze when no one is online.");
		config.updatePath("EnableTimeFreeze", "enable-time-freeze");
	}

	@Override
	public void initialize() {
		AddonHandler.logMessage(this.getName() + " Version " + this.getVersionString() + " Initializing...");
		TPATargets = new LinkedList<TPATargetClass>();
		TPARequests = new LinkedList<TPATargetClass>();
	}

	@Override
	public void handleConfigProperties(AddonConfig config) {
		enableTPA = config.getBoolean("enable-tpa");
		enableTimeFreeze = config.getBoolean("enable-time-freeze");
		if (enableTPA) {
			registerAddonCommand(new CommandTPA());
		}
		else {
			registerAddonCommand(new CommandBase() {
				@Override
				public String getCommandName() {
					return "tpa";
				}

				@Override
				public String getCommandUsage(ICommandSender iCommandSender) {
					return "/tpa is disabled.";
				}

				@Override
				public void processCommand(ICommandSender iCommandSender, String[] strings) {
					throw new CommandException("/tpa is disabled by default. If the server operator wants to enable this command, they'll configure it.");
				}
			});
		}

	}
	@Override
	public void postInitialize() {
		RecipeManager.removeVanillaShapelessRecipe(new ItemStack(BTWItems.wickerPane, 4), new Object[]{new ItemStack(BTWBlocks.wickerBasket)});
		RecipeManager.addShapelessRecipe(new ItemStack(BTWItems.wickerPane, 3), new Object[]{new ItemStack(BTWBlocks.wickerBasket)});
		RecipeManager.addSawRecipe(new ItemStack[]{new ItemStack(BTWItems.wickerPane, 6), new ItemStack(BTWItems.sawDust, 1)}, BTWBlocks.hamper);
	}

	public static void updateTPATargets() {
		for (TPATargetClass tpaTarget : TPATargets) {
			tpaTarget.update();
			if (tpaTarget.tickDuration <= 0 || tpaTarget.targetEntity == null || tpaTarget.teleportingEntity == null) {
				TPATargets.remove(tpaTarget);
			}
		}
	}
}