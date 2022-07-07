package nautilus.game.arcade.game.games.skywars.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemBuilder;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.LinearUpgradeKit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.game.games.skywars.kits.perks.PerkMagnetism;

public class KitMetal extends LinearUpgradeKit
{

	private static final ItemStack SKILL_ITEM = new ItemBuilder(Material.REDSTONE_COMPARATOR)
			.setTitle(C.cGreen + "Magnet")
			.build();

	private static final Perk[][] PERKS =
			{
					{
							new PerkMagnetism(SKILL_ITEM, 15000, 12, 1)
					},
					{
							new PerkMagnetism(SKILL_ITEM, 15000, 12, 1.1)
					},
					{
							new PerkMagnetism(SKILL_ITEM, 15000, 12, 1.2)
					},
					{
							new PerkMagnetism(SKILL_ITEM, 15000, 12, 1.3)
					},
					{
							new PerkMagnetism(SKILL_ITEM, 15000, 12, 1.4)
					},
					{
							new PerkMagnetism(SKILL_ITEM, 15000, 12, 1.5)
					},
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					new ItemStack(Material.WOOD_SWORD),
					new ItemStack(Material.STONE_PICKAXE),
					new ItemBuilder(Material.REDSTONE_COMPARATOR).setTitle(C.cGreen + "Magnet").build()
			};

	public KitMetal(ArcadeManager manager)
	{
		super(manager, GameKit.SKYWARS_METAL, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(PLAYER_ITEMS);
	}

}
