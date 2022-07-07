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
import nautilus.game.arcade.game.games.skywars.kits.perks.PerkIceBridge;

public class KitIce extends LinearUpgradeKit
{

	private static final ItemStack SKILL_ITEM = new ItemBuilder(Material.ICE)
			.setTitle(C.cGreen + "Ice Bridge")
			.build();

	private static final Perk[][] PERKS =
			{
					{
							new PerkIceBridge(SKILL_ITEM, 30000, 4000)
					},
					{
							new PerkIceBridge(SKILL_ITEM, 29000, 4000)
					},
					{
							new PerkIceBridge(SKILL_ITEM, 28000, 5000)
					},
					{
							new PerkIceBridge(SKILL_ITEM, 27000, 5000)
					},
					{
							new PerkIceBridge(SKILL_ITEM, 26000, 6000)
					},
					{
							new PerkIceBridge(SKILL_ITEM, 25000, 6000)
					},
			};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					new ItemStack(Material.WOOD_SWORD),
					new ItemBuilder(Material.ICE).setTitle(C.cGreen + "Ice Bridge").build()
			};

	public KitIce(ArcadeManager manager)
	{
		super(manager, GameKit.SKYWARS_ICE, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(PLAYER_ITEMS);
	}

}
