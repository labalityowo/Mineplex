package mineplex.core.gadget.gadgets.morph;

import java.util.concurrent.TimeUnit;

import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.common.util.C;
import mineplex.core.disguise.disguises.DisguiseSkeleton;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.morph.managers.UtilMorph;
import mineplex.core.gadget.util.CostConstants;
import mineplex.core.itemstack.ItemBuilder;

public class MorphSkeleton extends MorphAbilityGadget
{

	private static final ItemStack ACTIVE_ITEM = new ItemBuilder(Material.BOW)
			.setTitle(C.cGreenB + "Arrow Storm")
			.addLore("Clicking this fires out lots of high powered arrows!")
			.setUnbreakable(true)
			.build();

	public MorphSkeleton(GadgetManager manager)
	{
		super(manager, "Skeleton Morph", new String[]
				{
						C.cGray + "I have a bone to pick with you",
						C.blankLine,
						C.cGreen + "Shoot" + C.cWhite + " your " + C.cYellow + "Bow" + C.cWhite + " to fire",
						C.cGreen + "Arrow Storm"
				}, CostConstants.FOUND_IN_TREASURE_CHESTS, Material.SKULL_ITEM, (byte) 0, ACTIVE_ITEM, "Arrow Storm", TimeUnit.SECONDS.toMillis(8));
	}

	@Override
	public void enableCustom(Player player, boolean message)
	{
		applyArmor(player, message);

		DisguiseSkeleton disguise = new DisguiseSkeleton(player);
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
		Manager.runSyncTimer(new BukkitRunnable()
		{
			int arrows = 0;

			@Override
			public void run()
			{
				if (arrows++ == 10)
				{
					cancel();
					return;
				}

				player.launchProjectile(Arrow.class);
			}
		}, 0, 2);
	}
}
