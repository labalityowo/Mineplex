package mineplex.game.clans.clans.worldevent.boss.skeletonking.abilities;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilTime;
import mineplex.game.clans.clans.worldevent.api.BossPassive;
import mineplex.game.clans.clans.worldevent.api.EventCreatureDeathEvent;
import mineplex.game.clans.clans.worldevent.boss.skeletonking.SkeletonBoss;
import mineplex.game.clans.clans.worldevent.boss.skeletonking.SkeletonCreature;
import mineplex.game.clans.clans.worldevent.boss.skeletonking.minion.MinionType;
import mineplex.game.clans.clans.worldevent.boss.skeletonking.minion.UndeadArcherCreature;
import mineplex.game.clans.clans.worldevent.boss.skeletonking.minion.UndeadWarriorCreature;

public class SkeletonPassive extends BossPassive<SkeletonCreature, Skeleton>
{
	private static final int MAX_ARCHERS = 10;
	private static final int MAX_WARRIORS = 8;
	private static final long SPAWN_RATE = 5000;
	private List<Location> _queuedArchers = new ArrayList<>();
	private List<Location> _queuedWarriors = new ArrayList<>();
	private long _lastASpawned;
	private long _lastWSpawned;
	
	public SkeletonPassive(SkeletonCreature creature)
	{
		super(creature);
	}
	
	@Override
	public int getCooldown()
	{
		return 20;
	}
	
	@Override
	public boolean isProgressing()
	{
		return !_queuedArchers.isEmpty() || !_queuedWarriors.isEmpty();
	}

	@Override
	public void tick()
	{
		if (getBoss().Archers.size() < MAX_ARCHERS && _queuedArchers.isEmpty())
		{
			for (int i = 0; i < (MAX_ARCHERS - getBoss().Archers.size()); i++)
			{
				Location spawn = getLocation().clone();
				spawn.add(UtilMath.random(3, 6), 0, UtilMath.random(3, 6));
				_queuedArchers.add(spawn);
			}
		}
		if (getBoss().Warriors.size() < MAX_WARRIORS && _queuedWarriors.isEmpty())
		{
			for (int i = 0; i < (MAX_WARRIORS - getBoss().Warriors.size()); i++)
			{
				Location spawn = getLocation().clone();
				spawn.add(UtilMath.random(3, 6), 0, UtilMath.random(3, 6));
				_queuedWarriors.add(spawn);
			}
		}
		
		for (Location animate : _queuedArchers)
		{
			UtilParticle.PlayParticleToAll(ParticleType.BLOCK_DUST.getParticle(Material.DIRT, 0),
				animate.clone().add(0, 0.2, 0), null, 0, 4, ViewDist.NORMAL);
		}
		for (Location animate : _queuedWarriors)
		{
			UtilParticle.PlayParticleToAll(ParticleType.BLOCK_DUST.getParticle(Material.DIRT, 0),
					animate.clone().add(0, 0.2, 0), null, 0, 4, ViewDist.NORMAL);
		}
		
		if (!_queuedArchers.isEmpty() && UtilTime.elapsed(_lastASpawned, SPAWN_RATE))
		{
			_lastASpawned = System.currentTimeMillis();
			Location spawn = _queuedArchers.remove(0);
			getBoss().Archers.add((UndeadArcherCreature) ((SkeletonBoss)getBoss().getEvent()).spawnMinion(MinionType.ARCHER, spawn));
			
			UtilParticle.PlayParticleToAll(ParticleType.WITCH_MAGIC, spawn, null, 0, 2, ViewDist.MAX);
			UtilParticle.PlayParticleToAll(ParticleType.SMOKE, spawn, null, 0, 2, ViewDist.MAX);
			UtilParticle.PlayParticleToAll(ParticleType.WITCH_MAGIC, getEntity().getLocation(), null, 0, 2, ViewDist.MAX);
			UtilParticle.PlayParticleToAll(ParticleType.SMOKE, getEntity().getLocation(), null, 0, 2, ViewDist.MAX);
		}
		if (!_queuedWarriors.isEmpty() && UtilTime.elapsed(_lastWSpawned, SPAWN_RATE))
		{
			_lastWSpawned = System.currentTimeMillis();
			Location spawn = _queuedWarriors.remove(0);
			getBoss().Warriors.add((UndeadWarriorCreature) ((SkeletonBoss)getBoss().getEvent()).spawnMinion(MinionType.WARRIOR, spawn));
			
			UtilParticle.PlayParticleToAll(ParticleType.WITCH_MAGIC, spawn, null, 0, 2, ViewDist.MAX);
			UtilParticle.PlayParticleToAll(ParticleType.SMOKE, spawn, null, 0, 2, ViewDist.MAX);
			UtilParticle.PlayParticleToAll(ParticleType.WITCH_MAGIC, getEntity().getLocation(), null, 0, 2, ViewDist.MAX);
			UtilParticle.PlayParticleToAll(ParticleType.SMOKE, getEntity().getLocation(), null, 0, 2, ViewDist.MAX);
		}
	}
	
	@EventHandler
	public void onArcherDeath(EventCreatureDeathEvent event)
	{
		if (event.getCreature() instanceof UndeadArcherCreature)
		{
			getBoss().Archers.remove(event.getCreature());
		}
		if (event.getCreature() instanceof UndeadWarriorCreature)
		{
			getBoss().Warriors.remove(event.getCreature());
		}
	}
}