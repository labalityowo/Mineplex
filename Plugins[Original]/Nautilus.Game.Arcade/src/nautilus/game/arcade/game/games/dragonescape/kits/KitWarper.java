package nautilus.game.arcade.game.games.dragonescape.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkLeap;

public class KitWarper extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkLeap("Leap", 1, 1, 8000, 2)
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					ItemStackFactory.Instance.CreateStack(Material.IRON_AXE),
					ItemStackFactory.Instance.CreateStack(Material.ENDER_PEARL),
			};

	public KitWarper(ArcadeManager manager)
	{
		super(manager, GameKit.DRAGON_ESCAPE_WARPER, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(PLAYER_ITEMS);
		player.setExp(0.99f);
	}
}
