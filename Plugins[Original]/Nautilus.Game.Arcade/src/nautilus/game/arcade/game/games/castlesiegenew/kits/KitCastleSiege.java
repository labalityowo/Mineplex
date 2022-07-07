package nautilus.game.arcade.game.games.castlesiegenew.kits;

import java.lang.reflect.InvocationTargetException;

import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.disguise.DisguiseManager;
import mineplex.core.disguise.disguises.DisguiseInsentient;
import mineplex.core.game.kit.GameKit;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;

public abstract class KitCastleSiege extends Kit
{

	private static final ItemStack AXE = new ItemStack(Material.STONE_AXE);
	private static final ItemStack AXE_5 = new ItemStack(Material.IRON_AXE);

	KitCastleSiege(ArcadeManager manager, GameKit gameKit, Perk... perks)
	{
		super(manager, gameKit, perks);
	}

	public void disguise(Player player, Class<? extends DisguiseInsentient> clazz)
	{
		DisguiseManager disguiseManager = Manager.GetDisguise();

		try
		{
			DisguiseInsentient disguise = clazz.getConstructor(Entity.class).newInstance(player);
			GameTeam gameTeam = Manager.GetGame().GetTeam(player);

			if (gameTeam != null)
			{
				disguise.setName(gameTeam.GetColor() + player.getName());
			}
			else
			{
				disguise.setName(player.getName());
			}

			disguise.showArmor();
			disguise.setCustomNameVisible(true);

//			if (_witherSkeleton)
//			{
//				DisguiseSkeleton disguiseSkeleton = (DisguiseSkeleton) disguise;
//				disguiseSkeleton.SetSkeletonType(SkeletonType.WITHER);
//			}

			disguiseManager.disguise(disguise);
		}
		catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e)
		{
			e.printStackTrace();
		}
	}

	protected void giveItems(Player player)
	{
//		int level = getUpgradeLevel(player.getUniqueId());
//
//		switch (level)
//		{
//			case 5:
//				player.getInventory().addItem(AXE_5);
//				break;
//			default:
//				player.getInventory().addItem(AXE);
//				break;
//		}

		player.getInventory().addItem(AXE);
	}
}
