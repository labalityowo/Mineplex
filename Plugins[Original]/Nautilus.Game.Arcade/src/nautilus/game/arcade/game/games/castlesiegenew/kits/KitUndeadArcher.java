package nautilus.game.arcade.game.games.castlesiegenew.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.disguise.disguises.DisguiseSkeleton;
import mineplex.core.game.kit.GameKit;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkFletcher;
import nautilus.game.arcade.kit.perks.PerkIronSkin;

public class KitUndeadArcher extends KitCastleSiege
{

	private static final Perk[] PERKS =
			{
					new PerkFletcher(8, 2, true),
					new PerkIronSkin(1)
			};


	private static final ItemStack[] PLAYER_ITEMS =
			{
					new ItemStack(Material.BOW)
			};

	public static final ItemStack IN_HAND = new ItemStack(Material.BOW);

	public KitUndeadArcher(ArcadeManager manager)
	{
		super(manager, GameKit.CASTLE_SIEGE_UNDEAD_ARCHER, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		giveItems(player);
		player.getInventory().addItem(PLAYER_ITEMS);
		disguise(player, DisguiseSkeleton.class);
	}
}
