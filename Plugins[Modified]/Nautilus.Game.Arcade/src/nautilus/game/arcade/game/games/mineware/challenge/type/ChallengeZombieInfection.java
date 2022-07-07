package nautilus.game.arcade.game.games.mineware.challenge.type;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import mineplex.core.disguise.disguises.DisguiseVillager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.disguise.disguises.DisguiseZombie;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.arcade.game.games.mineware.BawkBawkBattles;
import nautilus.game.arcade.game.games.mineware.challenge.Challenge;
import nautilus.game.arcade.game.games.mineware.challenge.ChallengeType;
import nautilus.game.arcade.game.games.mineware.challenge.other.ZombieWrapper;

/**
 * A challenge based on zombie survival.
 * 
 * @deprecated
 */
public class ChallengeZombieInfection extends Challenge
{
	private ZombieWrapper _zombie;
	private Set<Player> _infected = new HashSet<>();
	private float _defaultSpeed = 1.3F;
	private float _speedIncrement = 0.2F;
	private float _speedLimit = 3.1F;
	private float _speedAfterInfection = 1.7F;

	public ChallengeZombieInfection(BawkBawkBattles host)
	{
		super(
			host,
			ChallengeType.LastStanding,
			"Zombie Infection",
			"Avoid the infected zombie.",
			"Infected humans can infect others!");

		Settings.setUseMapHeight();
	}

	@Override
	public ArrayList<Location> createSpawns()
	{
		ArrayList<Location> spawns = new ArrayList<Location>();

		for (Location location : circle(getCenter(), getArenaSize(10), 1, true, false, 0))
		{
			spawns.add(location.add(0, 1, 0));
		}

		return spawns;
	}

	@Override
	public void createMap()
	{
		for (Location location : circle(getCenter(), getArenaSize(15), 2, false, false, 0))
		{
			Block block = location.getBlock();

			if (location.getY() == getCenter().getY())
			{
				double chance = Math.random();

				if (chance < 0.5)
				{
					setBlock(block, Material.DIRT);

					if (UtilMath.random.nextBoolean())
					{
						setData(block, (byte) 1);
					}
				}
				else if (chance > 0.5 && chance < 0.8)
				{
					setBlock(block, Material.DIRT, (byte) 2);
				}
				else
				{
					setBlock(block, Material.GRASS);
				}
			}
			else if (location.getY() == 1 + getCenter().getY())
			{
				generateGrass(block, true);
			}

			addBlock(block);
		}
	}

	@Override
	public void onStart()
	{
		Host.DamageEvP = true;
		Host.DamagePvP = true;
		_zombie = new ZombieWrapper(this);
		spawnZombie();
	}

	@Override
	public void onEnd()
	{
		Host.DamageEvP = false;
		Host.DamagePvP = false;

		removeZombie();
		_infected.clear();
	}

	@EventHandler
	public void onUpdateZombie(UpdateEvent event)
	{
		if (!isChallengeValid())
			return;

		if (event.getType() == UpdateType.TICK)
		{
			if (_zombie.isFrozen())
			{
				unfreezeZombieWhenNeeded();
			}
			else if (_zombie.getLocation().getY() < 0)
			{
				_zombie.getEntity().teleport(getCenter());
			}
			else if (UtilMath.r(20) == 0)
			{
				freeze(UtilMath.r(2) + 1 * 1000);
			}
			else
			{
				selectTarget();
			}
		}
		else if (event.getType() == UpdateType.FAST)
		{
			increaseZombieSpeed();

			Player target = _zombie.getTarget();

			if (target != null)
			{
				if (UtilMath.offset2d(_zombie.getEntity(), target) <= 2)
				{
					damage(target);
				}
			}
		}
	}

	@EventHandler
	public void onCustomDamage(CustomDamageEvent event)
	{
		if (!isChallengeValid())
			return;

		Player damagee = event.GetDamageePlayer();

		if (!isPlayerValid(damagee))
			return;

		if (_infected.contains(event.GetDamageePlayer()))
		{
			event.SetCancelled("Infected");
		}
		else
		{
			if (event.GetDamage() > damagee.getHealth())
			{
				damagee.setHealth(0.01);	
				infect(damagee);
			}
			else if (!damagee.hasPotionEffect(PotionEffectType.SLOW) && !damagee.hasPotionEffect(PotionEffectType.CONFUSION))
			{
				damagee.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 20, 1));
				damagee.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 60, 1));
			}

			damagee.playSound(damagee.getLocation(), Sound.SPIDER_IDLE, 2.0F, 1.0F);
		}
	}

	@EventHandler
	public void onParticleUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;

		if (!isChallengeValid())
			return;

		showFlames(_zombie.getLocation());

		for (Player infected : _infected)
		{
			showFlames(infected.getLocation());
		}
	}

	@EventHandler
	public void onUpdateEndCheck(UpdateEvent event)
	{
		if (!isChallengeValid())
			return;

		if (event.getType() != UpdateType.FASTER)
			return;

		if (_infected.size() >= Settings.getMaxCompletedCount())
		{
			for (Player player : getPlayersAlive())
			{
				if (!_infected.contains(player))
				{
					setCompleted(player);
				}
			}
		}
	}

	@EventHandler
	public void onEntityCombust(EntityCombustEvent event)
	{
		if (!isChallengeValid())
			return;

		if (_zombie.getEntity().equals(event.getEntity()))
		{
			_zombie.extinguish();
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		if (!isChallengeValid())
			return;

		_infected.remove(event.getPlayer());
	}

	private void spawnZombie()
	{
		Host.CreatureAllow = true;
		_zombie.spawn();
		Host.CreatureAllow = false;

		for (Player player : Host.GetPlayers(false))
		{
			if (Host.getArcadeManager().GetDisguise().getActiveDisguise(player) instanceof DisguiseZombie)
			{
				Host.Manager.GetDisguise().undisguise(player);
			}
		}

		_zombie.setSpeed(_defaultSpeed);
		_zombie.extinguish();
	}

	private void removeZombie()
	{
		if (_zombie != null)
		{
			if (_zombie.getEntity() != null)
			{
				_zombie.remove();
			}
		}

		_zombie = null;
	}

	private void unfreezeZombieWhenNeeded()
	{
		if (System.currentTimeMillis() > _zombie.getFreezeTime())
		{
			_zombie.unfreeze();
		}
	}

	private void selectTarget()
	{
		Player target = UtilPlayer.getClosest(_zombie.getLocation(), (Entity) null);

		while (_infected.contains(target))
		{
			target = UtilMath.randomElement(getPlayersAlive());
		}

		_zombie.setTarget(target);
		_zombie.move(target);
	}

	private void increaseZombieSpeed()
	{
		float increasedSpeed = _zombie.getSpeed() + _speedIncrement;

		if (increasedSpeed <= _speedLimit)
		{
			_zombie.setSpeed(increasedSpeed);
		}
		else
		{
			_zombie.setSpeed(_defaultSpeed);
		}
	}

	private void showFlames(Location loc)
	{
		UtilParticle.PlayParticle(ParticleType.FLAME, loc.add(0, 1.5, 0), 0.3F, 0.3F, 0.3F, 0.01F, 1, ViewDist.MAX, UtilServer.getPlayers());
	}

	private void freeze(long duration)
	{
		_zombie.setFreezeTime(System.currentTimeMillis() + duration);
		_zombie.freeze();
	}

	private void infect(Player player)
	{
		if (!_infected.contains(player))
		{
			player.setHealth(20.0);
			_infected.add(player);
			_zombie.setSpeed(_speedAfterInfection);

			Host.WorldData.World.strikeLightningEffect(player.getLocation());
			Host.getArcadeManager().GetDisguise().disguise(new DisguiseZombie(player));

			if (_infected.size() < Settings.getMaxCompletedCount()) // Check if the challenge is still running after the player is infected.
			{
				UtilPlayer.message(player, F.main("Game", "You have been infected, you now have to infect the rest."));
			}
		}
	}

	private void damage(Player player)
	{
		Host.Manager.GetDamage().NewDamageEvent(player, _zombie.getEntity(), null, DamageCause.ENTITY_ATTACK, 5.0, false, false, false, "Attack", "Infection");
	}

}
