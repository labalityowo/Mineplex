package nautilus.game.arcade.kit.perks;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Color;
import org.bukkit.DyeColor;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.particles.effects.LineParticle;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.disguise.disguises.DisguiseBase;
import mineplex.core.disguise.disguises.DisguiseSheep;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.kit.Perk;

public class PerkLazer extends Perk
{

	private static final float MAX_CHARGE = 0.99F;
	private static final float INCREMENTATION = 0.2F;

	private long _recharge;
	private float _chargePerTick;
	private float _hitBoxRadius;
	private float _damageRadius;
	private int _damage;
	private float _knockbackMagnitude;
	private double _range;

	private final Set<UUID> _active = new HashSet<>();

	public PerkLazer()
	{
		super("Static Laser", new String[] { C.cYellow + "Hold Block" + C.cGray + " with Sword to use " + C.cGreen + "Static Laser" });
	}

	public PerkLazer(String name, long recharge, float chargePerTick, float hitBoxRadius, float damageRadius, int damage, float knockbackMagnitude, double range)
	{
		super(name);

		_recharge = recharge;
		_chargePerTick = chargePerTick;
		_hitBoxRadius = hitBoxRadius;
		_damageRadius = damageRadius;
		_damage = damage;
		_knockbackMagnitude = knockbackMagnitude;
		_range = range;
	}

	@Override
	public void setupValues()
	{
		_recharge = getPerkTime("Cooldown");
		_chargePerTick = getPerkFloat("Charge Per Tick");
		_hitBoxRadius = getPerkFloat("Hit Box Radius");
		_damageRadius = getPerkFloat("Damage Radius");
		_damage = getPerkInt("Damage");
		_knockbackMagnitude = getPerkFloat("Knockback Magnitude");
		_range = getPerkInt("Range");
	}

	@EventHandler
	public void skill(PlayerInteractEvent event)
	{
		if (event.isCancelled())
		{
			return;
		}

		if (!UtilEvent.isAction(event, ActionType.R))
		{
			return;
		}

		if (UtilBlock.usable(event.getClickedBlock()))
		{
			return;
		}

		Player player = event.getPlayer();

		if (!UtilItem.isSword(player.getItemInHand()))
		{
			return;
		}

		if (!hasPerk(player))
		{
			return;
		}

		if (!Recharge.Instance.use(player, GetName(), _recharge, true, true))
		{
			return;
		}

		_active.add(player.getUniqueId());
	}

	@EventHandler
	public void chargeFire(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}
		
		Iterator<UUID> playerIterator = _active.iterator();

		while (playerIterator.hasNext())
		{
			UUID uuid = playerIterator.next();
			Player player = UtilPlayer.searchExact(uuid);

			if (player == null)
			{
				playerIterator.remove();
				continue;
			}

			if (player.isBlocking())
			{
				player.setExp(Math.min(MAX_CHARGE, player.getExp() + _chargePerTick));

				player.getWorld().playSound(player.getLocation(), Sound.FIZZ, 0.25f + player.getExp(), 0.75f + player.getExp());

				// Wool
				DisguiseBase disguise = Manager.GetDisguise().getActiveDisguise(player);
				if (disguise != null && disguise instanceof DisguiseSheep)
				{
					DisguiseSheep sheep = (DisguiseSheep) disguise;

					if (Math.random() > 0.5)
					{
						sheep.setColor(DyeColor.YELLOW);
					}
					else
					{
						sheep.setColor(DyeColor.BLACK);
					}

					sheep.setSheared(false);

					sheep.UpdateDataWatcher();
					Manager.GetDisguise().updateDisguise(disguise);
				}

				if (player.getExp() >= MAX_CHARGE)
				{
					playerIterator.remove();
					fire(player);
				}
			}
			else
			{
				playerIterator.remove();
				fire(player);
			}
		}
	}

	public void fire(Player player)
	{
		if (player.getExp() <= 0.2)
		{
			setWoolColor(player, DyeColor.WHITE);
			player.setExp(0f);
			return;
		}

		LineParticle lineParticle = new LineParticle(player.getEyeLocation(), player.getLocation().getDirection(), INCREMENTATION, _range * player.getExp(), ParticleType.FIREWORKS_SPARK,
				UtilServer.getPlayers());

		particleLoop: while (!lineParticle.update())
		{
			for (Player other : UtilPlayer.getNearby(lineParticle.getLastLocation().subtract(0, 1, 0), _hitBoxRadius))
			{
				if (player.equals(other))
				{
					continue;
				}
				
				break particleLoop;
			}
		}

		Location target = lineParticle.getDestination();

		UtilParticle.PlayParticle(ParticleType.EXPLODE, target, 0, 0, 0, 0, 1, ViewDist.MAX, UtilServer.getPlayers());

		// Firework
		UtilFirework.playFirework(target, Type.BURST, Color.YELLOW, false, false);

		for (LivingEntity other : UtilEnt.getInRadius(target, _damageRadius).keySet())
		{
			if (other.equals(player))
			{
				continue;
			}

			// Damage Event
			Manager.GetDamage().NewDamageEvent(other, player, null, DamageCause.CUSTOM, player.getExp() * _damage, true, true, false, player.getName(), GetName());
		}

		// Inform
		UtilPlayer.message(player, F.main("Game", "You fired " + F.skill(GetName()) + "."));

		// Sound
		player.getWorld().playSound(player.getEyeLocation(), Sound.ZOMBIE_REMEDY, 0.5f + player.getExp(), 1.75f - player.getExp());
		player.getWorld().playSound(player.getLocation(), Sound.SHEEP_IDLE, 2f, 1.5f);

		// Wool
		setWoolColor(player, DyeColor.WHITE);
		player.setExp(0f);
	}

	@EventHandler
	public void knockback(CustomDamageEvent event)
	{
		if (event.GetReason() == null || !event.GetReason().contains(GetName()))
		{
			return;
		}

		event.AddKnockback(GetName(), _knockbackMagnitude);
	}

	private void setWoolColor(Player player, DyeColor color)
	{
		DisguiseBase disguise = Manager.GetDisguise().getActiveDisguise(player);

		if (disguise != null && disguise instanceof DisguiseSheep)
		{
			DisguiseSheep sheep = (DisguiseSheep) disguise;
			sheep.setSheared(false);
			sheep.setColor(color);

			sheep.UpdateDataWatcher();
			Manager.GetDisguise().updateDisguise(disguise);
		}
	}
}
