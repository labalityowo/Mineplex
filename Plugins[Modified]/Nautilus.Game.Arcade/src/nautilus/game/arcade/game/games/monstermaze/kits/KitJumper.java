package nautilus.game.arcade.game.games.monstermaze.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.monstermaze.kits.perks.PerkJumpsDisplay;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkDummy;

public class KitJumper extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkDummy("Jumper", new String[]{C.cGray + "You can jump " + C.cYellow + "5 Times" + C.cGray + "."}),
					new PerkJumpsDisplay()
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					ItemStackFactory.Instance.CreateStack(Material.COMPASS, (byte) 0, 1, F.item("Safe Pad Locator")),
					ItemStackFactory.Instance.CreateStack(Material.FEATHER, (byte) 0, 5, C.cYellow + C.Bold + 5 + " Jumps Remaining")
			};

	public KitJumper(ArcadeManager manager)
	{
		super(manager, GameKit.MONSTER_MAZE_JUMPER, PERKS );
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().setItem(4, PLAYER_ITEMS[0]);
		player.getInventory().setItem(8, PLAYER_ITEMS[1]);
	}
}
