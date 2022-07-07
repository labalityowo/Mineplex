package nautilus.game.arcade.game.games.micro.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;

public class KitWorker extends Kit
{

	private static final ItemStack[] PLAYER_ITEMS =
			{
					ItemStackFactory.Instance.CreateStack(Material.WOOD_SWORD),
					ItemStackFactory.Instance.CreateStack(Material.STONE_SPADE),
					ItemStackFactory.Instance.CreateStack(Material.STONE_PICKAXE),
					ItemStackFactory.Instance.CreateStack(Material.STONE_AXE),
					ItemStackFactory.Instance.CreateStack(Material.APPLE, 4)
			};

	public KitWorker(ArcadeManager manager)
	{
		super(manager, GameKit.MICRO_WORKER);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(PLAYER_ITEMS);
	}
}
