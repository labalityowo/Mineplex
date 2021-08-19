package nautilus.game.arcade.game.games.sheep.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkThrower;

public class KitBrute extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkThrower()
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					ItemStackFactory.Instance.CreateStack(Material.IRON_SWORD),
					ItemStackFactory.Instance.CreateStack(Material.SADDLE, (byte) 0, 1, C.cYellow + C.Bold + "Hold This" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Grab/Hold Sheep")
			};

	public KitBrute(ArcadeManager manager)
	{
		super(manager, GameKit.SHEEP_QUEST_BRUTE, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(PLAYER_ITEMS);
	}
}
