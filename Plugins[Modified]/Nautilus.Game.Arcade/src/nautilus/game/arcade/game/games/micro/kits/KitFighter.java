package nautilus.game.arcade.game.games.micro.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkIronSkin;

public class KitFighter extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkIronSkin(0.5)
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					ItemStackFactory.Instance.CreateStack(Material.WOOD_SWORD),
					ItemStackFactory.Instance.CreateStack(Material.APPLE, 5)
			};

	public KitFighter(ArcadeManager manager)
	{
		super(manager, GameKit.MICRO_FIGHTER, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(PLAYER_ITEMS);
	}
}
