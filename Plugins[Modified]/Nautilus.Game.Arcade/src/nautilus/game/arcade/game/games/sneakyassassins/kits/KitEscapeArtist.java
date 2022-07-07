package nautilus.game.arcade.game.games.sneakyassassins.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.game.kit.GameKit;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkSmokebomb;

public class KitEscapeArtist extends SneakyAssassinKit
{

	private static final Perk[] PERKS =
			{
					new PerkSmokebomb(Material.INK_SACK, 3, true),
			};

	public KitEscapeArtist(ArcadeManager manager)
	{
		super(manager, GameKit.SNEAKY_ASSASSINS_ESCAPE_ARTIST, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		super.GiveItems(player);

		ItemStack inkSack = player.getInventory().getItem(player.getInventory().first(Material.INK_SACK));
		inkSack.setAmount(inkSack.getAmount() + 2);
	}
}
