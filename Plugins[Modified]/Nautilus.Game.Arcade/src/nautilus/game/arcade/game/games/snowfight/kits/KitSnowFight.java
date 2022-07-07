package nautilus.game.arcade.game.games.snowfight.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemBuilder;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;

class KitSnowFight extends Kit
{

	private static final ItemStack WINTER_STEW = new ItemBuilder(Material.MUSHROOM_SOUP)
			.setTitle(C.cYellow + "Hearty Winter Strew")
			.build();

	KitSnowFight(ArcadeManager manager, GameKit gameKit, Perk... perks)
	{
		super(manager, gameKit, perks);
	}

	@Override
	public void GiveItems(Player player)
	{
		for (int i = 2; i < 5; i++)
		{
			player.getInventory().setItem(i, WINTER_STEW);
		}
	}
}
