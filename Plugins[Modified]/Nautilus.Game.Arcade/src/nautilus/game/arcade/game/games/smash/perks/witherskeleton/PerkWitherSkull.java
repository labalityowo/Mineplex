package nautilus.game.arcade.game.games.smash.perks.witherskeleton;

import java.util.HashSet;
import java.util.Set;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.game.games.smash.perks.SmashPerk;

public class PerkWitherSkull extends SmashPerk
{

	private static final ItemStack HELMET = new ItemStack(Material.SKULL_ITEM, 1, (short) 0, (byte) 1);

	private int _cooldownNormal;
	private int _cooldownSmash;
	private int _damage;
	private float _skullVelocity, _knockbackMagnitude;

	private final Set<SkullData> _active = new HashSet<>();

	public PerkWitherSkull()
	{
		super("Wither Skull", new String[] {C.cYellow + "Hold Block" + C.cGray + " to use " + C.cGreen + "Wither Skull"});
	}

	@Override
	public void setupValues()
	{
		_cooldownNormal = getPerkTime("Cooldown Normal");
		_cooldownSmash = getPerkTime("Cooldown Smash");
		_damage = getPerkInt("Damage");
		_skullVelocity = getPerkFloat("Skull Velocity");
		_knockbackMagnitude = getPerkFloat("Knockback Magnitude");
	}

	@EventHandler(priority = EventPriority.LOW) // Happen BEFORE super is
	// triggered
	public void activate(PlayerInteractEvent event)
	{
		if (event.isCancelled() || UtilBlock.usable(event.getClickedBlock()))
		{
			return;
		}

		Player player = event.getPlayer();
		boolean smash = isSuperActive(player);

		if (!smash && (!UtilItem.isSword(player.getItemInHand()) || !UtilEvent.isAction(event, ActionType.R)))
		{
			return;
		}

		if (!hasPerk(player) || !Recharge.Instance.use(player, GetName(), smash ? _cooldownSmash : _cooldownNormal, !smash, !smash))
		{
			return;
		}

		Manager.GetGame().CreatureAllowOverride = true;
		Location location = player.getLocation();
		ArmorStand skull = player.getWorld().spawn(location, ArmorStand.class);
		skull.setHelmet(HELMET);
		skull.setGravity(false);
		skull.setVisible(false);
		_active.add(new SkullData(player, skull, location.getDirection().multiply(_skullVelocity), !smash));
		Manager.GetGame().CreatureAllowOverride = false;

		player.getWorld().playSound(player.getLocation(), Sound.WITHER_SHOOT, 1f, 1f);

		// Inform
		if (!smash)
		{
			UtilPlayer.message(player, F.main("Skill", "You launched " + F.skill(GetName()) + "."));
		}
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void cleanAndControl(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		_active.removeIf(skullData ->
		{
			Player shooter = skullData.Shooter;
			ArmorStand skullHolder = skullData.SkullHolder;

			if (!skullHolder.isValid())
			{
				return true;
			}

			if (skullData.Control && shooter.isBlocking())
			{
				skullData.Direction = shooter.getLocation().getDirection();
			}

			Location newLocation = skullHolder.getLocation().add(skullData.Direction.clone().multiply(1.6));
			newLocation.setYaw(UtilAlg.GetYaw(skullData.Direction));
			skullHolder.teleport(newLocation);
			UtilParticle.PlayParticleToAll(ParticleType.SMOKE, newLocation.add(0, 2, 0), null, 0.01F, 2, ViewDist.LONG);

			boolean hitEntity = false, hitBlock = UtilBlock.solid(skullHolder.getLocation().add(0, 1.5, 0).getBlock());

			if (!hitBlock)
			{
				for (LivingEntity entity : UtilEnt.getInRadius(newLocation, 1).keySet())
				{
					if (entity.equals(shooter) || entity.equals(skullHolder))
					{
						continue;
					}

					hitEntity = true;
					break;
				}
			}

			if (hitEntity || hitBlock)
			{
				explode(skullData, hitBlock);
				return true;
			}

			return false;
		});
	}

	private void explode(SkullData skullData, boolean adjustY)
	{
		ArmorStand skullHolder = skullData.SkullHolder;
		Location location = skullHolder.getLocation();
		double scale = 0.4 + 0.6 * Math.min(1, skullHolder.getTicksLived() / 20d);

		if (adjustY)
		{
			location.add(0, 2, 0);
		}

		UtilPlayer.getInRadius(location, 6).forEach((player, scale1) ->
		{
			if (player.equals(skullData.Shooter))
			{
				return;
			}

			Manager.GetDamage().NewDamageEvent(player, skullData.Shooter, null, DamageCause.CUSTOM, _damage * scale * scale1, true, true, false, skullData.Shooter.getName(), GetName());
		});

		location.getWorld().playSound(location, Sound.EXPLODE, 2.5F, 0.4F);
		UtilParticle.PlayParticleToAll(ParticleType.HUGE_EXPLOSION, location, null, 0, 1, ViewDist.LONG);
		skullHolder.remove();
	}

	@EventHandler(priority = EventPriority.HIGH)
	public void knockback(CustomDamageEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}

		for (SkullData data : _active)
		{
			if (data.SkullHolder.equals(event.GetDamageeEntity()))
			{
				event.SetCancelled("Wither Skull Holder");
				return;
			}
		}

		if (event.GetReason() == null || !event.GetReason().contains(GetName()))
		{
			return;
		}

		event.AddKnockback(GetName(), _knockbackMagnitude);
	}

	private class SkullData
	{

		Player Shooter;
		ArmorStand SkullHolder;
		Vector Direction;
		boolean Control;

		SkullData(Player shooter, ArmorStand skullHolder, Vector direction, boolean control)
		{
			Shooter = shooter;
			SkullHolder = skullHolder;
			Direction = direction;
			Control = control;
		}
	}
}
