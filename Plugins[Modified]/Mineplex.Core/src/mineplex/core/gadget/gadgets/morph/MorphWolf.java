package mineplex.core.gadget.gadgets.morph;

import java.util.concurrent.TimeUnit;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAction;
import mineplex.core.disguise.disguises.DisguiseWolf;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.morph.managers.UtilMorph;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.recharge.Recharge;

public class MorphWolf extends MorphAbilityGadget
{

	private static final ItemStack ACTIVE_ITEM = new ItemBuilder(Material.BONE)
			.setTitle(C.cGreenB + "Pounce")
			.addLore("Clicking this causes you to pounce.")
			.setUnbreakable(true)
			.build();

	public MorphWolf(GadgetManager manager)
	{
		super(manager, "Wolf Morph", new String[]
				{
						C.cGray + "Arf",
						C.blankLine,
						C.cGreen + "Click" + C.cWhite + " your " + C.cYellow + "Bone" + C.cWhite + " to " + C.cGreen + "Pounce"
				}, CostConstants.FOUND_IN_TREASURE_CHESTS, Material.BONE, (byte) 0, ACTIVE_ITEM, "Pounce", TimeUnit.SECONDS.toMillis(2));
	}

	@Override
	public void enableCustom(Player player, boolean message)
	{
		applyArmor(player, message);

		DisguiseWolf disguise = new DisguiseWolf(player);
		UtilMorph.disguise(player, disguise, Manager);
	}

	@Override
	public void disableCustom(Player player, boolean message)
	{
		removeArmor(player);

		UtilMorph.undisguise(player, Manager.getDisguiseManager());
	}

	@Override
	public void onAbilityActivate(Player player)
	{
		player.getWorld().playSound(player.getLocation(), Sound.WOLF_BARK, 1, 0.8F);
		UtilAction.velocity(player, 1.6, 0.2, 1, true);
	}

	@EventHandler
	public void playerSneak(PlayerToggleSneakEvent event)
	{
		if (!event.isSneaking())
		{
			return;
		}

		Player player = event.getPlayer();

		if (!isActive(player) || !Recharge.Instance.use(player, "Wolf Howl", 8000, true, false))
		{
			return;
		}

		player.getWorld().playSound(player.getLocation(), Sound.WOLF_HOWL, 1, (float) (0.4 + Math.random() / 2));
	}
}
