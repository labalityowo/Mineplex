package nautilus.game.arcade.game.games.runner.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkQuickshot;

public class KitArcher extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkQuickshot("Quickshot", 1.2, 6000)
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					ItemStackFactory.Instance.CreateStack(Material.BOW)
			};

	public KitArcher(ArcadeManager manager)
	{
		super(manager, GameKit.RUNNER_ARCHER, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(PLAYER_ITEMS);
	}
}
