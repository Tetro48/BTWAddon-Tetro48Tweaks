package btw.community.tweaks;

import btw.AddonHandler;
import btw.BTWAddon;
import btw.block.BTWBlocks;
import btw.crafting.recipe.RecipeManager;
import btw.item.BTWItems;
import net.minecraft.server.MinecraftServer;
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
    public void preInitialize() {
        registerProperty("EnableTPA", "False", "This toggles the /tpa command.");
        registerProperty("EnableTimeFreeze", "True", "This toggles the time freeze when no one is online.");
    }

    @Override
    public void initialize() {
        AddonHandler.logMessage(this.getName() + " Version " + this.getVersionString() + " Initializing...");
        TPATargets = new LinkedList<TPATargetClass>();
        TPARequests = new LinkedList<TPATargetClass>();
    }

    @Override
    public void handleConfigProperties(Map<String, String> propertyValues) {
        enableTPA = Boolean.parseBoolean(propertyValues.get("EnableTPA"));
        enableTimeFreeze = Boolean.parseBoolean(propertyValues.get("EnableTimeFreeze"));
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
            if (tpaTarget.tickDuration < 0 || tpaTarget.targetEntity == null || tpaTarget.teleportingEntity == null) {
                TPATargets.remove(tpaTarget);
            }
        }
    }
}