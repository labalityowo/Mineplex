package mineplex.game.clans.clans.worldevent.boss.skeletonking.abilities;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.bukkit.Location;
import org.bukkit.entity.Skeleton;

import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilTime;
import mineplex.game.clans.clans.worldevent.api.BossAbility;
import mineplex.game.clans.clans.worldevent.boss.skeletonking.SkeletonBoss;
import mineplex.game.clans.clans.worldevent.boss.skeletonking.SkeletonCreature;
import mineplex.game.clans.clans.worldevent.boss.skeletonking.minion.MinionType;

public class SkeletonHellishFlood extends BossAbility<SkeletonCreature, Skeleton>
{
	private static final int WAVE_COUNT = 3;
	private static final int WAVE_SIZE = 5;
	private static final MinionType[] POSSIBLE_MINIONS = new MinionType[] {MinionType.WARRIOR, MinionType.ARCHER};
	private static final long WAVE_DELAY = 1000;
	
	private Map<String, MinionType[]> _waves = new HashMap<>();
	private long _lastSpawned;
	private int _current;
	private int _ticks;
	
	public SkeletonHellishFlood(SkeletonCreature creature)
	{
		super(creature);
		
		if (WAVE_COUNT > 0)
		{
			for (int i = 1; i <= WAVE_COUNT; i++)
			{
				createWave(i);
			}
		}
		_lastSpawned = System.currentTimeMillis();
		_current = 1;
	}
	
	private void createWave(int number)
	{
		int length = POSSIBLE_MINIONS.length;
		if (length <= 0 || WAVE_SIZE <= 0)
		{
			return;
		}
		MinionType[] wave = new MinionType[WAVE_SIZE];
		for (int i = 0; i < WAVE_SIZE; i++)
		{
			wave[i] = POSSIBLE_MINIONS[new Random().nextInt(length)];
		}
		_waves.put("Wave " + number, wave);
	}
	
	@Override
	public int getCooldown()
	{
		return 30;
	}

	@Override
	public boolean canMove()
	{
		return false;
	}

	@Override
	public boolean inProgress()
	{
		return true;
	}

	@Override
	public boolean hasFinished()
	{
		return _waves.isEmpty() && _ticks > ((WAVE_DELAY / 1000) * 20 * (WAVE_COUNT - 1));
	}

	@Override
	public void setFinished()
	{
		_waves.clear();
		_ticks = 60;
	}

	@Override
	public void tick()
	{
		_ticks++;
		if (UtilTime.elapsed(_lastSpawned, WAVE_DELAY))
		{
			if (_current <= WAVE_COUNT)
			{
				for (MinionType type : _waves.get("Wave " + _current))
				{
					Location toSpawn = getLocation().clone();
					toSpawn.add(UtilMath.random(3, 6), 0, UtilMath.random(3, 6));
					
					((SkeletonBoss)getBoss().getEvent()).spawnMinion(type, toSpawn);
					
					UtilParticle.PlayParticleToAll(ParticleType.WITCH_MAGIC, toSpawn, null, 0, 2, ViewDist.MAX);
					UtilParticle.PlayParticleToAll(ParticleType.SMOKE, toSpawn, null, 0, 2, ViewDist.MAX);
				}
				UtilParticle.PlayParticleToAll(ParticleType.WITCH_MAGIC, getEntity().getLocation(), null, 0, 2, ViewDist.MAX);
				UtilParticle.PlayParticleToAll(ParticleType.SMOKE, getEntity().getLocation(), null, 0, 2, ViewDist.MAX);
				_waves.remove("Wave " + _current);
				_current++;
				_lastSpawned = System.currentTimeMillis();
			}
		}
	}
}