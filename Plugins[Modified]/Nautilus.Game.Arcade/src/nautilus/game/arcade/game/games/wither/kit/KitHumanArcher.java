package nautilus.game.arcade.game.games.wither.kit;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.itemstack.ItemStackFactory;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkDoubleJump;
import nautilus.game.arcade.kit.perks.PerkFletcher;
import nautilus.game.arcade.kit.perks.PerkWitherArrowBlind;

public class KitHumanArcher extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkDoubleJump("Double Jump", 1.2, 1, true, 6000, true),
					new PerkWitherArrowBlind(6),
					new PerkFletcher(4, 4, true),
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					ItemStackFactory.Instance.CreateStack(Material.IRON_SWORD),
					ItemStackFactory.Instance.CreateStack(Material.BOW),
					new ItemBuilder(Material.POTION).setAmount(2).setData((short) 16429).setTitle(C.Reset + "Revival Potion").build(),
					ItemStackFactory.Instance.CreateStack(Material.MUSHROOM_SOUP),
					ItemStackFactory.Instance.CreateStack(Material.MUSHROOM_SOUP),
			};

	public KitHumanArcher(ArcadeManager manager)
	{
		super(manager, GameKit.WITHER_ASSAULT_ARCHER, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(PLAYER_ITEMS);
	}

}
