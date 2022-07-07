package nautilus.game.arcade.game.games.smash.perks.witch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Bat;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
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
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.recharge.Recharge;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

import nautilus.game.arcade.game.games.smash.perks.SmashPerk;

public class PerkBatWave extends SmashPerk
{

	private static final int LEASH_COOLDOWN = 500;

	private int _cooldown;
	private int _hitCooldown;
	private int _hitBox;
	private int _damage;
	private int _disableDamage;
	private float _knockbackMagnitude;
	
	private static final String LEASH = "Leash Bats";
	private static final String HIT = "Hit By Bat";

	private Map<UUID, Long> _active = new HashMap<>();
	private Map<UUID, Location> _direction = new HashMap<>();
	private Map<UUID, List<Bat>> _bats = new HashMap<>();
	private Map<UUID, Double> _damageTaken = new HashMap<>();
	private Set<UUID> _pulling = new HashSet<>();
	private Set<UUID> _allowLeash = new HashSet<>();

	public PerkBatWave()
	{
		super("Bat Wave", new String[] { C.cYellow + "Right-Click" + C.cGray + " with Spade to use " + C.cGreen + "Bat Wave", C.cYellow + "Double Right-Click" + C.cGray + " with Spade to use "
				+ C.cGreen + "Bat Leash" });
	}

	@Override
	public void setupValues()
	{
		_cooldown = getPerkTime("Cooldown");
		_hitCooldown = getPerkInt("Hit Cooldown (ms)");
		_hitBox = getPerkInt("Hit Box");
		_damage = getPerkInt("Damage");
		_disableDamage = getPerkInt("Disable Damage");
		_knockbackMagnitude = getPerkFloat("Knockback Magnitude");
	}

	@EventHandler
	public void Activate(PlayerInteractEvent event)
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

		if (!UtilItem.isSpade(player.getItemInHand()))
		{
			return;
		}

		if (!hasPerk(player))
		{
			return;
		}

		if (isSuperActive(player))
		{
			return;
		}

		UUID key = player.getUniqueId();

		if (!Recharge.Instance.use(player, GetName(), _cooldown, false, true))
		{
			if (_active.containsKey(key))
			{
				if (!Recharge.Instance.use(player, LEASH, LEASH_COOLDOWN, false, false))
				{
					return;
				}

				if (!_pulling.remove(key))
				{
					if (_allowLeash.remove(key))
					{
						_pulling.add(key);

						for (Bat bat : _bats.get(key))
						{
							bat.setLeashHolder(player);
						}
					}
				}
				else
				{
					for (Bat bat : _bats.get(key))
					{
						bat.setLeashHolder(null);
					}
				}
			}
			else
			{
				// Inform
				Recharge.Instance.use(player, GetName(), _cooldown, true, true);
			}
		}
		else
		{
			// Start
			_direction.put(key, player.getEyeLocation());
			_active.put(key, System.currentTimeMillis());
			_allowLeash.add(key);

			_bats.put(key, new ArrayList<Bat>());

			Manager.GetGame().CreatureAllowOverride = true;

			for (int i = 0; i < 32; i++)
			{
				Bat bat = player.getWorld().spawn(player.getEyeLocation(), Bat.class);
				_bats.get(key).add(bat);
			}

			Manager.GetGame().CreatureAllowOverride = false;

			// Inform
			UtilPlayer.message(player, F.main("Skill", "You used " + F.skill(GetName()) + "."));
		}
	}

	@EventHandler
	public void Update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
		{
			return;
		}

		for (Player cur : UtilServer.getPlayers())
		{
			UUID key = cur.getUniqueId();

			if (!_active.containsKey(key))
			{
				continue;
			}

			if (UtilTime.elapsed(_active.get(key), 2500))
			{
				Clear(cur);
				continue;
			}

			Location loc = _direction.get(key);

			Vector batVec = new Vector(0, 0, 0);
			double batCount = 0;

			// Bat Movement
			for (Bat bat : _bats.get(key))
			{
				if (!bat.isValid())
				{
					continue;
				}

				batVec.add(bat.getLocation().toVector());
				batCount++;

				Vector rand = new Vector((Math.random() - 0.5) / 2, (Math.random() - 0.5) / 2, (Math.random() - 0.5) / 2);
				bat.setVelocity(loc.getDirection().clone().multiply(0.5).add(rand));

				for (Player other : Manager.GetGame().GetPlayers(true))
				{
					if (other.equals(cur))
					{
						continue;
					}
					
					if (!Recharge.Instance.usable(other, HIT))
					{
						continue;
					}

					if (UtilEnt.hitBox(bat.getLocation(), other, _hitBox, null))
					{
						// Damage Event
						Manager.GetDamage().NewDamageEvent(other, cur, null, DamageCause.CUSTOM, _damage, true, true, false, cur.getName(), GetName());

						// Effect
						bat.getWorld().playSound(bat.getLocation(), Sound.BAT_HURT, 1f, 1f);
						UtilParticle.PlayParticle(ParticleType.LARGE_SMOKE, bat.getLocation(), 0, 0, 0, 0, 3, ViewDist.LONG, UtilServer.getPlayers());

						bat.remove();

						// Recharge on hit
						Recharge.Instance.useForce(other, HIT, _hitCooldown);
					}
				}
			}

			// Player Pull
			if (_pulling.contains(key))
			{
				batVec.multiply(1 / batCount);

				Location batLoc = batVec.toLocation(cur.getWorld());

				UtilAction.velocity(cur, UtilAlg.getTrajectory(cur.getLocation(), batLoc), 0.35, false, 0, 0, 10, false);
			}
		}
	}

	@EventHandler
	public void PlayerQuit(PlayerQuitEvent event)
	{
		Clear(event.getPlayer());
	}

	@EventHandler
	public void PlayerDeath(PlayerDeathEvent event)
	{
		Clear(event.getEntity());
	}

	public void Clear(Player player)
	{
		UUID key = player.getUniqueId();

		_active.remove(key);
		_direction.remove(key);
		_pulling.remove(key);
		_damageTaken.remove(key);
		
		if (_bats.containsKey(key))
		{
			for (Bat bat : _bats.get(key))
			{
				if (bat.isValid())
				{
					UtilParticle.PlayParticle(ParticleType.LARGE_SMOKE, bat.getLocation(), 0, 0, 0, 0, 3, ViewDist.LONG, UtilServer.getPlayers());
				}

				bat.remove();
			}

			_bats.remove(player.getUniqueId());
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void damage(CustomDamageEvent event)
	{
		if (event.GetDamageePlayer() != null)
		{
			Player player = event.GetDamageePlayer();
			UUID key = player.getUniqueId();

			_damageTaken.putIfAbsent(key, 0D);
			
			if (hasPerk(player) && _damageTaken.containsKey(key) && _pulling.contains(key))
			{				
				_damageTaken.put(key, (_damageTaken.get(key) + event.GetDamage()));
				
				if (event.GetCause() == DamageCause.ENTITY_ATTACK || _damageTaken.get(key) >= _disableDamage)
				{
					Clear(player);
				}
			}
		}
		
		if (event.GetReason() == null || !event.GetReason().contains(GetName()))
		{
			return;
		}

		event.AddKnockback(GetName(), _knockbackMagnitude);
	}
}
