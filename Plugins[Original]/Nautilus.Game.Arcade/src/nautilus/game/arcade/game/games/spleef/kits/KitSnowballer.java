package nautilus.game.arcade.game.games.spleef.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkKnockback;

public class KitSnowballer extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkKnockback(0.3)
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					ItemStackFactory.Instance.CreateStack(Material.SNOW_BALL),
			};

	public KitSnowballer(ArcadeManager manager)
	{
		super(manager, GameKit.SPLEEF_SNOWBALLER, PERKS);

	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(PLAYER_ITEMS);
	}
}
