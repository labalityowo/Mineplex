package nautilus.game.arcade.game.games.spleef.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkFletcher;
import nautilus.game.arcade.kit.perks.PerkKnockback;

public class KitArcher extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkFletcher(2, 2, false),
					new PerkKnockback(0.3)
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					ItemStackFactory.Instance.CreateStack(Material.BOW),
			};

	public KitArcher(ArcadeManager manager)
	{
		super(manager, GameKit.SPLEEF_ARCHER, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(PLAYER_ITEMS);
	}
}
