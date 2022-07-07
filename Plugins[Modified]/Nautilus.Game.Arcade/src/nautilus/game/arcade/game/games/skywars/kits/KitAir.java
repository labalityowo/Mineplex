package nautilus.game.arcade.game.games.skywars.kits;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemBuilder;

import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.LinearUpgradeKit;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkDoubleJump;
import nautilus.game.arcade.game.games.skywars.kits.perks.PerkVoidSaver;

public class KitAir extends LinearUpgradeKit
{

	private static final String DOUBLE_JUMP = "Leap";

	private static final ItemStack SKILL_ITEM = new ItemBuilder(Material.EYE_OF_ENDER)
			.setTitle(C.cGreen + "Eye of Ender")
			.build();

	private static final Perk[][] PERKS =
		{
			{
				new PerkVoidSaver(SKILL_ITEM),
				new PerkDoubleJump(DOUBLE_JUMP, 1, 1, true, 25000, true)
			},
			{
				new PerkVoidSaver(SKILL_ITEM),
				new PerkDoubleJump(DOUBLE_JUMP, 1, 1, true, 24000, true)
			},
			{
				new PerkVoidSaver(SKILL_ITEM),
				new PerkDoubleJump(DOUBLE_JUMP, 1.1, 1, true, 23000, true)
			},
			{
				new PerkVoidSaver(SKILL_ITEM),
				new PerkDoubleJump(DOUBLE_JUMP, 1.1, 1, true, 22000, true)
			},
			{
				new PerkVoidSaver(SKILL_ITEM),
				new PerkDoubleJump(DOUBLE_JUMP, 1.2, 1, true, 21000, true)
			},
			{
				new PerkVoidSaver(SKILL_ITEM),
				new PerkDoubleJump(DOUBLE_JUMP, 1.2, 1, true, 20000, true)
			},
		};

	private static final ItemStack[] PLAYER_ITEMS =
			{
					new ItemStack(Material.WOOD_SWORD),
					new ItemBuilder(Material.EYE_OF_ENDER).setTitle(C.cGreen + "Eye of Ender").build()
			};

	public KitAir(ArcadeManager manager)
	{
		super(manager, GameKit.SKYWARS_AIR, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.getInventory().addItem(PLAYER_ITEMS);
	}

	@EventHandler
	public void fallDamage(CustomDamageEvent event)
	{
		if (event.GetCause() != DamageCause.FALL)
		{
			return;
		}

		Player player = event.GetDamageePlayer();

		if (HasKit(player))
		{
			event.SetCancelled("Air Kit Fall Damage");
		}
	}
}
