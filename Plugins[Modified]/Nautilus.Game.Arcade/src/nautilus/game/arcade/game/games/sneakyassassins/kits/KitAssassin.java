package nautilus.game.arcade.game.games.sneakyassassins.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkSmokebomb;

public class KitAssassin extends SneakyAssassinKit
{

	private static final Perk[] PERKS =
			{
					new PerkSmokebomb(Material.INK_SACK, 3, true),
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					ItemStackFactory.Instance.CreateStack(Material.BOW),
					ItemStackFactory.Instance.CreateStack(Material.ARROW, 32)
			};

	public KitAssassin(ArcadeManager manager)
	{
		super(manager, GameKit.SNEAKY_ASSASSINS_ASSASSIN, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		super.GiveItems(player);

		player.getInventory().addItem(PLAYER_ITEMS);
	}
}
