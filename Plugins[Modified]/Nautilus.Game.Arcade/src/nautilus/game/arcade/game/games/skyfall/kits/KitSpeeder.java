package nautilus.game.arcade.game.games.skyfall.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.game.kit.GameKit;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.skyfall.kits.perks.PerkIncreaseBoosters;
import nautilus.game.arcade.game.games.skyfall.kits.perks.PerkSlowDown;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;

/**
 * Kit which allows Players
 * to use {@link PerkIncreaseBoosters}.
 *
 * @author xXVevzZXx
 */
public class KitSpeeder extends Kit
{

	private static final String SPEED = "Ring Boost";
	
	private static final Perk[] PERKS =
		{
				new PerkSlowDown("Slow Down", 10),
				new PerkIncreaseBoosters(SPEED, 1.1)
		};

	private static final ItemStack PLAYER_ITEM = new ItemStack(Material.ELYTRA);

	public KitSpeeder(ArcadeManager manager)
	{
		super(manager, GameKit.SKYFALL_SPEEDER, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().setChestplate(PLAYER_ITEM);
	}
}
