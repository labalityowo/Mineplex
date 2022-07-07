package nautilus.game.arcade.game.games.skywars.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemBuilder;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.skywars.kits.perks.PerkDirtCannon;
import nautilus.game.arcade.kit.LinearUpgradeKit;
import nautilus.game.arcade.kit.Perk;

public class KitEarth extends LinearUpgradeKit
{

	private static final ItemStack SKILL_ITEM = new ItemBuilder(Material.DIRT)
			.setTitle(C.cGreen + "Throwable Dirt")
			.setGlow(true)
			.build();

	private static final Perk[][] PERKS =
		{
			{
				new PerkDirtCannon(SKILL_ITEM,1),

			},
			{
				new PerkDirtCannon(SKILL_ITEM,1.1),

			},
			{
				new PerkDirtCannon(SKILL_ITEM,1.2),

			},
			{
				new PerkDirtCannon(SKILL_ITEM,1.3),

			},
			{
				new PerkDirtCannon(SKILL_ITEM,1.4),

			},
			{
				new PerkDirtCannon(SKILL_ITEM,1.5),

			},
		};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					new ItemStack(Material.WOOD_SWORD),
					new ItemStack(Material.WOOD_SPADE)
			};


	public KitEarth(ArcadeManager manager)
	{
		super(manager, GameKit.SKYWARS_EARTH, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(PLAYER_ITEMS);
	}

}
