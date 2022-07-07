package nautilus.game.arcade.game.games.lobbers.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.F;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.lobbers.kits.perks.PerkCraftman;
import nautilus.game.arcade.game.games.lobbers.kits.perks.PerkWaller;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkDoubleJump;

public class KitWaller extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkDoubleJump("Double Jump", 1.2, 1.2, false),
					new PerkWaller(),
					new PerkCraftman()
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					ItemStackFactory.Instance.CreateStack(Material.STONE_SPADE, (byte) 0, 3, F.item("Wall Builder"))
			};

	public KitWaller(ArcadeManager manager)
	{
		super(manager, GameKit.BOMB_LOBBERS_WALLER, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().setItem(1, PLAYER_ITEMS[0]);
	}

}

