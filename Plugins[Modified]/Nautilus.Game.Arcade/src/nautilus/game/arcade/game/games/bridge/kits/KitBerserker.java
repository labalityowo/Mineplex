package nautilus.game.arcade.game.games.bridge.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkAxeman;
import nautilus.game.arcade.kit.perks.PerkLeap;

public class KitBerserker extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkLeap("Beserker Leap", 1.2, 1.2, 10000),
					new PerkAxeman(),
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					ItemStackFactory.Instance.CreateStack(Material.STONE_AXE)
			};

	public KitBerserker(ArcadeManager manager)
	{
		super(manager, GameKit.BRIDGES_BERSERKER, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(PLAYER_ITEMS);
	}
}