package nautilus.game.arcade.game.games.dragonescape.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.F;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkDisruptor;
import nautilus.game.arcade.kit.perks.PerkLeap;

public class KitDisruptor extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkLeap("Leap", 1, 1, 8000, 3),
					new PerkDisruptor(8, 2)
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					ItemStackFactory.Instance.CreateStack(Material.IRON_AXE),
					ItemStackFactory.Instance.CreateStack(Material.TNT, (byte) 0, 1, F.item("Disruptor"))
			};

	public KitDisruptor(ArcadeManager manager)
	{
		super(manager, GameKit.DRAGON_ESCAPE_DISRUPTOR, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(PLAYER_ITEMS);
		player.setExp(0.99f);
	}
}
