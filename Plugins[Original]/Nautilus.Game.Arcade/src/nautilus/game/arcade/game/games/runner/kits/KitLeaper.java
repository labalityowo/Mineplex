package nautilus.game.arcade.game.games.runner.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkLeap;

public class KitLeaper extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkLeap("Leap", 1.2, 1.2, 8000)
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					ItemStackFactory.Instance.CreateStack(Material.IRON_AXE)
			};

	public KitLeaper(ArcadeManager manager)
	{
		super(manager, GameKit.RUNNER_LEAPER, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(PLAYER_ITEMS);
	}
}
