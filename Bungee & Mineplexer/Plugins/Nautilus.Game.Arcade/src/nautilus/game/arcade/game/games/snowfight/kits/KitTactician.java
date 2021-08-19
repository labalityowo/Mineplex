package nautilus.game.arcade.game.games.snowfight.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemBuilder;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.snowfight.perks.PerkStoneWall;
import nautilus.game.arcade.game.modules.compass.CompassModule;
import nautilus.game.arcade.kit.Perk;

public class KitTactician extends KitSnowFight
{

	private static final Perk[] PERKS =
			{
					new PerkStoneWall("Ice Wall", Material.ICE, Material.CLAY_BALL),
			};

	private static final ItemStack PLAYER_ITEM = new ItemBuilder(Material.CLAY_BALL)
			.setTitle(C.cYellow + "Ice Wall")
			.build();

	public KitTactician(ArcadeManager manager)
	{
		super(manager, GameKit.SNOW_FIGHT_TACTICIAN, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().setItem(1, PLAYER_ITEM);
		player.getInventory().setItem(7, CompassModule.getCompassItem());

		super.GiveItems(player);
	}
}
