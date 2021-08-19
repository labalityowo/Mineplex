package nautilus.game.arcade.game.games.smash.perks.cow;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Cow;
import org.bukkit.entity.MushroomCow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilEvent;
import mineplex.core.common.util.UtilEvent.ActionType;
import mineplex.core.common.util.UtilItem;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.smash.perks.SmashPerk;

public class PerkCowAngryHerd extends SmashPerk
{

	private int _cooldownNormal;
	private int _cooldownSmash;
	private int _maxTime;
	private int _stuckTime;
	private int _forceMove;
	private float _hitBoxRadius;
	private int _hitFrequency;
	private int _damage;

	private List<DataCowCharge> _active = new ArrayList<>();

	public PerkCowAngryHerd()
	{
		super("Angry Herd", new String[] { C.cYellow + "Right-Click" + C.cGray + " with Axe to use " + C.cGreen + "Angry Herd" });
	}

	@Override
	public void setupValues()
	{
		_cooldownNormal = getPerkTime("Cooldown Normal");
		_cooldownSmash = getPerkTime("Cooldown Smash");
		_maxTime = getPerkInt("Max Time (ms)");
		_stuckTime = getPerkInt("Stuck Time (ms)");
		_forceMove = getPerkInt("Force Move (ms)");
		_hitBoxRadius = getPerkFloat("Hit Box Radius");
		_hitFrequency = getPerkInt("Hit Frequency (ms)");
		_damage = getPerkInt("Damage");
	}

	@EventHandler
	public void shoot(PlayerInteractEvent event)
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

		if (!UtilItem.isAxe(player.getItemInHand()))
		{
			return;
		}

		if (!hasPerk(player))
		{
			return;
		}

		if (!Recharge.Instance.use(player, GetName(), isSuperActive(player) ? _cooldownSmash : _cooldownNormal, true, true))
		{
			return;
		}

		event.setCancelled(true);

		for (double i = -2; i < 3; i++)
		{
			Vector dir = player.getLocation().getDirection();
			dir.setY(0);
			dir.normalize();

			Location loc = player.getLocation();

			loc.add(dir);
			loc.add(UtilAlg.getLeft(dir).multiply(i * 1.5));

			Manager.GetGame().CreatureAllowOverride = true;
			Class<? extends Cow> clazz = isSuperActive(player) ? MushroomCow.class : Cow.class;
			Cow cow = player.getWorld().spawn(loc, clazz);
			Manager.GetGame().CreatureAllowOverride = false;

			_active.add(new DataCowCharge(player, cow));
		}

		// Sound
		player.getWorld().playSound(player.getLocation(), Sound.COW_IDLE, 2f, 0.6f);

		// Inform
		UtilPlayer.message(player, F.main("Game", "You used " + F.skill(GetName()) + "."));
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		Iterator<DataCowCharge> activeIter = _active.iterator();

		while (activeIter.hasNext())
		{
			DataCowCharge data = activeIter.next();

			// Expire
			if (UtilTime.elapsed(data.Time, _maxTime))
			{
				if (data.Cow.isValid())
				{
					data.Cow.remove();
					UtilParticle.PlayParticleToAll(ParticleType.EXPLODE, data.Cow.getLocation().add(0, 1, 0), 0, 0, 0, 0, 1, ViewDist.NORMAL);
				}

				activeIter.remove();
				continue;
			}

			// Set Moved
			if (UtilMath.offset(data.Cow.getLocation(), data.LastLoc) > 1)
			{
				data.LastLoc = data.Cow.getLocation();
				data.LastMoveTime = System.currentTimeMillis();
			}

			// Stuck Remove
			if (UtilTime.elapsed(data.LastMoveTime, _stuckTime))
			{
				if (data.Cow.isValid())
				{
					data.Cow.remove();
					UtilParticle.PlayParticleToAll(ParticleType.EXPLODE, data.Cow.getLocation().add(0, 1, 0), 0, 0, 0, 0, 1, ViewDist.NORMAL);
				}

				activeIter.remove();
				continue;
			}

			// Gravity
			if (UtilEnt.isGrounded(data.Cow))
			{
				data.Direction.setY(-0.1);
			}
			else
			{
				data.Direction.setY(Math.max(-1, data.Direction.getY() - 0.03));
			}

			// Move
			if (UtilTime.elapsed(data.LastMoveTime, _forceMove) && UtilEnt.isGrounded(data.Cow))
			{
				data.Cow.setVelocity(data.Direction.clone().add(new Vector(0, 0.75, 0)));
			}
			else
			{
				data.Cow.setVelocity(data.Direction);
			}

			if (Math.random() > 0.99)
			{
				data.Cow.getWorld().playSound(data.Cow.getLocation(), Sound.COW_IDLE, 1f, 1f);
			}

			if (Math.random() > 0.97)
			{
				data.Cow.getWorld().playSound(data.Cow.getLocation(), Sound.COW_WALK, 1f, 1.2f);
			}
			
			// Hit
			for (Player player : Manager.GetGame().GetPlayers(true))
			{
				if (player.equals(data.Player))
				{
					continue;
				}

				if (UtilMath.offset(player, data.Cow) < _hitBoxRadius)
				{
					if (Recharge.Instance.use(player, "Hit by " + data.Player.getName(), _hitFrequency, false, false))
					{
						// Damage Event
						Manager.GetDamage().NewDamageEvent(player, data.Player, null, data.Cow.getLocation(), DamageCause.CUSTOM, _damage, true, true, false, UtilEnt.getName(data.Player), GetName());

						UtilParticle.PlayParticleToAll(ParticleType.LARGE_EXPLODE, data.Cow.getLocation().add(0, 1, 0), 1f, 1f, 1f, 0, 12, ViewDist.LONG);

						data.Cow.getWorld().playSound(data.Cow.getLocation(), Sound.ZOMBIE_WOOD, 0.75f, 0.8f);
						data.Cow.getWorld().playSound(data.Cow.getLocation(), Sound.COW_HURT, 1.5f, 0.75f);
					}
				}
			}
		}
	}

	@EventHandler
	public void Knockback(CustomDamageEvent event)
	{
		if (event.GetReason() == null || !event.GetReason().contains(GetName()))
		{
			return;
		}
		
		event.AddKnockback(GetName(), 1.25);
	}
}
