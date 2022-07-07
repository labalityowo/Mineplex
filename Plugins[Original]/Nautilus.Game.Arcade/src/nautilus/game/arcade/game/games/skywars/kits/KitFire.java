package nautilus.game.arcade.game.games.skywars.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.C;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemBuilder;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.games.skywars.kits.perks.PerkFireBurst;
import nautilus.game.arcade.kit.LinearUpgradeKit;
import nautilus.game.arcade.kit.Perk;

public class KitFire extends LinearUpgradeKit
{

	private static final ItemStack SKILL_ITEM = new ItemBuilder(Material.BLAZE_ROD)
			.setTitle(C.cGreen + "Fire Burst")
			.build();

	private static final Perk[][] PERKS =
		{
			{
				new PerkFireBurst(SKILL_ITEM, 30000, 6, 6)
			},
			{
				new PerkFireBurst(SKILL_ITEM, 29000, 6, 6)
			},
			{
				new PerkFireBurst(SKILL_ITEM, 28000, 6, 6)
			},
			{
				new PerkFireBurst(SKILL_ITEM, 27000, 6, 6)
			},
			{
				new PerkFireBurst(SKILL_ITEM, 26000, 6, 6)
			},
			{
				new PerkFireBurst(SKILL_ITEM, 25000, 6, 6)
			},
		};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					new ItemStack(Material.WOOD_SWORD),
					new ItemBuilder(Material.BLAZE_ROD).setTitle(C.cGreen + "Fire Burst").build()
			};

	private static final PotionEffect FIRE_RESISTANCE = new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false);

	public KitFire(ArcadeManager manager)
	{
		super(manager, GameKit.SKYWARS_FIRE, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(PLAYER_ITEMS);
		player.addPotionEffect(FIRE_RESISTANCE);
	}
	

	
}
