package mineplex.game.clans.clans.worldevent.raid.wither.creature.mage;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Skeleton;

import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilTime;
import mineplex.game.clans.clans.worldevent.api.BossPassive;
import mineplex.game.clans.clans.worldevent.raid.wither.WitherRaid;

public class MageSummon extends BossPassive<UndeadMage, Skeleton>
{
	private final int MAX_KNIGHTS;
	private static final int KNIGHTS_PER_USE = 6;
	private long _lastUsed;
	private List<Location> _spawnLocations;
	
	public MageSummon(UndeadMage creature, List<Location> spawnLocations)
	{
		super(creature);
		
		_lastUsed = System.currentTimeMillis();
		MAX_KNIGHTS = spawnLocations == null ? 10 : spawnLocations.size();
		_spawnLocations = spawnLocations;
	}
	
	private void spawnKnight()
	{
		Location spawn = null;
		if (_spawnLocations != null)
		{
			spawn = UtilMath.randomElement(_spawnLocations);
		}
		else
		{
			spawn = getLocation();
		}
		getBoss().getEvent().registerCreature(new UndeadKnight(getBoss().getChallenge(), spawn));
		UtilParticle.PlayParticleToAll(ParticleType.WITCH_MAGIC, spawn, null, 0, 2, ViewDist.MAX);
		UtilParticle.PlayParticleToAll(ParticleType.SMOKE, spawn, null, 0, 2, ViewDist.MAX);
		UtilParticle.PlayParticleToAll(ParticleType.WITCH_MAGIC, getLocation(), null, 0, 2, ViewDist.MAX);
		UtilParticle.PlayParticleToAll(ParticleType.SMOKE, getLocation(), null, 0, 2, ViewDist.MAX);
	}
	
	@Override
	public int getCooldown()
	{
		return 30;
	}
	
	@Override
	public boolean isProgressing()
	{
		return false;
	}

	@Override
	public void tick()
	{
		if (!UtilTime.elapsed(_lastUsed, getCooldown() * 1000))
		{
			return;
		}
		long amount = ((WitherRaid)getBoss().getEvent()).getCreatures().stream().filter(UndeadKnight.class::isInstance).count(); 
		if (amount < MAX_KNIGHTS)
		{
			_lastUsed = System.currentTimeMillis();
			long spawnAmount = Math.min(MAX_KNIGHTS - amount, KNIGHTS_PER_USE);
			for (int i = 0; i < spawnAmount; i++)
			{
				spawnKnight();
			}
		}
	}
}