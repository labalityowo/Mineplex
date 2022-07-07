package nautilus.game.arcade.game.games.tug.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.UtilItem.ItemCategory;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemBuilder;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.smash.perks.golem.PerkSeismicSlam;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.Perk;

public class KitTugLeaper extends Kit
{

	private static final ItemStack[] PLAYER_ITEMS =
			{
					new ItemBuilder(Material.IRON_AXE)
							.setUnbreakable(true)
							.build()
			};

	private static final ItemStack[] PLAYER_ARMOUR =
			{
					new ItemBuilder(Material.GOLD_BOOTS)
							.setUnbreakable(true)
							.build(),
					new ItemBuilder(Material.GOLD_LEGGINGS)
							.setUnbreakable(true)
							.build(),
					new ItemBuilder(Material.GOLD_CHESTPLATE)
							.setUnbreakable(true)
							.build(),
					new ItemBuilder(Material.GOLD_HELMET)
							.setUnbreakable(true)
							.build()
			};

	private static final Perk[] PERKS =
			{
					new PerkSeismicSlam("Seismic Slam", 5000, 250, 10, 6, 1, ItemCategory.AXE),
			};

	public KitTugLeaper(ArcadeManager manager)
	{
		super(manager, GameKit.TUG_LEAPER, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(PLAYER_ITEMS);
		player.getInventory().setArmorContents(PLAYER_ARMOUR);
	}
}
