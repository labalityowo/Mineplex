package nautilus.game.arcade.game.games.castlesiegenew.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.disguise.disguises.DisguiseZombie;
import mineplex.core.game.kit.GameKit;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkRegeneration;

public class KitUndeadZombie extends KitCastleSiege
{

	private static final Perk[] PERKS =
			{
					new PerkRegeneration(2)
			};

	public static final ItemStack IN_HAND = new ItemStack(Material.STONE_AXE);

	public KitUndeadZombie(ArcadeManager manager)
	{
		super(manager, GameKit.CASTLE_SIEGE_UNDEAD_ZOMBIE, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		giveItems(player);
		disguise(player, DisguiseZombie.class);
	}
}
