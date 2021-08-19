package nautilus.game.arcade.game.games.milkcow.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkDoubleJump;

public class KitFarmerJump extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkDoubleJump("Double Jump", 1, 0.8, false)
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					ItemStackFactory.Instance.CreateStack(Material.BUCKET)
			};

	public KitFarmerJump(ArcadeManager manager)
	{
		super(manager, GameKit.MILK_THE_COW_LEAP, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(PLAYER_ITEMS);
	}
}
