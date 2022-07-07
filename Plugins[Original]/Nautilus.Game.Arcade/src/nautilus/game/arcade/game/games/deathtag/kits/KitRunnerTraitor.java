package nautilus.game.arcade.game.games.deathtag.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkKnockback;

public class KitRunnerTraitor extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkKnockback(0.8)
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					ItemStackFactory.Instance.CreateStack(Material.IRON_AXE)
			};

	public KitRunnerTraitor(ArcadeManager manager)
	{
		super(manager, GameKit.DEATH_TAG_TRAITOR, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(PLAYER_ITEMS);
	}
}
