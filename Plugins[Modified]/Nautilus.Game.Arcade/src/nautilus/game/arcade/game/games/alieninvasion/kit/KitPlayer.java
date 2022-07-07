package nautilus.game.arcade.game.games.alieninvasion.kit;

import mineplex.core.common.util.C;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemBuilder;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkDoubleJump;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class KitPlayer extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkDoubleJump("Leap", 1, 1, true, 10000, true),
					new PerkBlaster()
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					new ItemBuilder(Material.DIAMOND_BARDING)
							.setTitle(C.cBlue + C.Scramble + "ABC " + C.cAqua + "Super Snow Blaster 3000" + C.cBlue + C.Scramble + " ABC")
							.build()
			};

	public KitPlayer(ArcadeManager manager)
	{
		super(manager, GameKit.ALIEN_INVASION_PLAYER, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(PLAYER_ITEMS);
	}
}
