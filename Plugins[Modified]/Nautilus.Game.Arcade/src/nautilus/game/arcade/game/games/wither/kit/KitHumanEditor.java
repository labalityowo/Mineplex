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

public class KitHumanEditor extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkDoubleJump("Double Jump", 1, 0.8, true, 6000, true),
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					new ItemStack(Material.STONE_AXE),
					new ItemStack(Material.STONE_PICKAXE),
					new ItemStack(Material.STONE_SPADE),
					new ItemBuilder(Material.POTION).setAmount(2).setData((short) 16429).setTitle(C.Reset + "Revival Potion").build(),
					ItemStackFactory.Instance.CreateStack(Material.MUSHROOM_SOUP),
					ItemStackFactory.Instance.CreateStack(Material.MUSHROOM_SOUP),
			};

	public KitHumanEditor(ArcadeManager manager)
	{
		super(manager, GameKit.WITHER_ASSAULT_EDITOR, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(PLAYER_ITEMS);
	}
}
