package nautilus.game.arcade.game.games.valentines.kit;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;

public class KitMasterOfLove extends Kit
{

	private static final ItemStack[] PLAYER_ITEMS =
			{
					ItemStackFactory.Instance.CreateStack(Material.FISHING_ROD, (byte) 0, 1, "Cupids Pig Catcher")
			};

	public KitMasterOfLove(ArcadeManager manager)
	{
		super(manager, GameKit.VALENTINES_PLAYER);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(PLAYER_ITEMS);
	}
}
