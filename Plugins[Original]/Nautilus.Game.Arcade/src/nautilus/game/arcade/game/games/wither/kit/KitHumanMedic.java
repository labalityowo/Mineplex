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
import nautilus.game.arcade.kit.perks.PerkBlockRestorer;
import nautilus.game.arcade.kit.perks.PerkDoubleJump;
import nautilus.game.arcade.kit.perks.PerkWitherMedicRefill;

public class KitHumanMedic extends Kit
{

	private static final Perk[] PERKS =
			{
					new PerkDoubleJump("Double Jump", 1, 0.8, true, 6000, true),
					new PerkWitherMedicRefill(45, 1),
					new PerkBlockRestorer()
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					new ItemStack(Material.STONE_AXE),
					new ItemBuilder(Material.POTION).setAmount(2).setData((short) 16429).setTitle(C.Reset + "Revival Potion").build(),
					ItemStackFactory.Instance.CreateStack(Material.MUSHROOM_SOUP),
					ItemStackFactory.Instance.CreateStack(Material.MUSHROOM_SOUP),
			};

	public KitHumanMedic(ArcadeManager manager)
	{
		super(manager, GameKit.WITHER_ASSAULT_MEDIC, PERKS);

	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(PLAYER_ITEMS);
	}
}
