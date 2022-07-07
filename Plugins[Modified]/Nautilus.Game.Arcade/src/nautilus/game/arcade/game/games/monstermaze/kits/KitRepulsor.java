package nautilus.game.arcade.game.games.monstermaze.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.monstermaze.kits.perks.PerkRepulsor;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;

public class KitRepulsor extends Kit
{
	private static final Perk[] PERKS =
			{
					new PerkRepulsor()
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					ItemStackFactory.Instance.CreateStack(Material.COAL, (byte) 0, 3, C.cYellow + C.Bold + "Right Click" + C.cWhite + C.Bold + " - " + C.cGreen + C.Bold + "Repulse"),
					ItemStackFactory.Instance.CreateStack(Material.COMPASS, (byte) 0, 1, F.item("Safe Pad Locator"))
			};

	public KitRepulsor(ArcadeManager manager)
	{
		super(manager, GameKit.MONSTER_MAZE_REPULSOR, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().setItem(0, PLAYER_ITEMS[0]);
		player.getInventory().setItem(4, PLAYER_ITEMS[1]);
	}
}
