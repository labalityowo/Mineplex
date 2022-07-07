package nautilus.game.arcade.game.games.micro.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkFletcher;

public class KitArcher extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkFletcher(20, 3, true)
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					ItemStackFactory.Instance.CreateStack(Material.WOOD_SWORD),
					ItemStackFactory.Instance.CreateStack(Material.BOW),
					ItemStackFactory.Instance.CreateStack(Material.APPLE, 3)
			};

	public KitArcher(ArcadeManager manager)
	{
		super(manager, GameKit.MICRO_ARCHER, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(PLAYER_ITEMS);
	}
}
