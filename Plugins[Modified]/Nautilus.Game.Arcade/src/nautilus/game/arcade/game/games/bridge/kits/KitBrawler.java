package nautilus.game.arcade.game.games.bridge.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkIronSkin;
import nautilus.game.arcade.kit.perks.PerkMammoth;

public class KitBrawler extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkMammoth(),
					new PerkIronSkin(1)
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					ItemStackFactory.Instance.CreateStack(Material.IRON_SWORD)
			};

	public KitBrawler(ArcadeManager manager)
	{
		super(manager, GameKit.BRIDGES_BRAWLER, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(PLAYER_ITEMS);
	}
}
