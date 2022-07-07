package nautilus.game.arcade.game.games.castlesiegenew.kits;

import java.util.concurrent.TimeUnit;

import org.bukkit.Effect;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.disguise.disguises.DisguiseWolf;
import mineplex.core.game.kit.GameKit;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.recharge.Recharge;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.kit.Perk;
import nautilus.game.arcade.kit.perks.PerkKnockbackGive;
import nautilus.game.arcade.kit.perks.PerkStrength;

public class KitHumanWolf extends KitCastleSiege
{


	private static final Perk[] PERKS =
			{
					new PerkStrength(1),
					new PerkKnockbackGive(2)
			};

	private static final long COOLDOWN = TimeUnit.SECONDS.toMillis(80);
	private static final String WOLF_BITE = "Wolf Bite";
	private static final ItemStack[] PLAYER_ITEMS =
			{
					new ItemBuilder(Material.BONE)
							.setTitle(C.cYellowB + WOLF_BITE)
							.addLore("Hitting any Undead will do 3 Hearts of damage!", "80 second cooldown.")
							.build()
			};

	public static final ItemStack IN_HAND = new ItemStack(Material.IRON_HOE);

	public KitHumanWolf(ArcadeManager manager)
	{
		super(manager, GameKit.CASTLE_SIEGE_WOLF, PERKS);
	}

	@Override
	public void GiveItems(Player player)
	{
		player.setHealth(5);
		player.getInventory().addItem(PLAYER_ITEMS);
		disguise(player, DisguiseWolf.class);
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void damage(CustomDamageEvent event)
	{
		if (event.isCancelled() || !Manager.GetGame().IsLive())
		{
			return;
		}

		LivingEntity damagee = event.GetDamageeEntity();
		Player damager = event.GetDamagerPlayer(false);

		if (damager == null || damager.getItemInHand() == null || damager.getItemInHand().getType() != Material.BONE || !Recharge.Instance.usable(damager, WOLF_BITE))
		{
			return;
		}

		Recharge.Instance.useForce(damager, WOLF_BITE, COOLDOWN, true);
		damager.sendMessage(F.main("Game", "You used " + F.skill(WOLF_BITE) + " on " + F.name(damagee.getName()) + "."));
		damager.getWorld().playSound(damager.getLocation(), Sound.WOLF_BARK, 1, 0.6F);
		damagee.getWorld().playEffect(damagee.getLocation().add(0, 0.5, 0), Effect.STEP_SOUND, Material.REDSTONE_BLOCK);
		Manager.GetDamage().NewDamageEvent(damagee, damager, null, DamageCause.CUSTOM, 3, true, true, true, damager.getName(), WOLF_BITE);
	}
}
