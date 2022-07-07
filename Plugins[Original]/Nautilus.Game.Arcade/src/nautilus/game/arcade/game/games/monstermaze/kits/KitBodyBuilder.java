package nautilus.game.arcade.game.games.monstermaze.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.F;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemStackFactory;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkDummy;

public class KitBodyBuilder extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkDummy("Body Builder", new String[]{"Your " + F.elem("Max Health") + " increases by " + F.skill("One Heart"), "when you are first to a Safe Pad.", "Maximum of 15 hearts."})
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					ItemStackFactory.Instance.CreateStack(Material.COMPASS, (byte) 0, 1, F.item("Safe Pad Locator"))
			};

	public KitBodyBuilder(ArcadeManager manager)
	{
		super(manager, GameKit.MONSTER_MAZE_BODY_BUILDER, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().setItem(4, PLAYER_ITEMS[0]);
	}
}
