package mineplex.game.clans.clans.worldevent.raid.wither.creature.mage;

import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Skeleton;

import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.game.clans.clans.worldevent.api.BossPassive;

public class MageBlink extends BossPassive<UndeadMage, Skeleton>
{
	private List<Location> _spots;
	private long _lastUsed;
	
	public MageBlink(UndeadMage creature, List<Location> teleportSpots)
	{
		super(creature);
		
		_spots = teleportSpots;
		_lastUsed = System.currentTimeMillis();
	}
	
	@Override
	public int getCooldown()
	{
		return 7;
	}
	
	@Override
	public boolean isProgressing()
	{
		return false;
	}

	@Override
	public void tick()
	{
		if (UtilTime.elapsed(_lastUsed, getCooldown() * 1000))
		{
			_lastUsed = System.currentTimeMillis();
			List<Location> tp = null;
			if (_spots != null)
			{
				tp = _spots.stream().filter(spot -> UtilMath.offset(getLocation(), spot) <= 10).collect(Collectors.toList());
			}
			else
			{
				tp = getBoss().getPlayers(UtilPlayer.getInRadius(getLocation(), 10), 10).stream().map(Player::getLocation).collect(Collectors.toList());
			}
			if (tp != null && !tp.isEmpty())
			{
				Location to = UtilMath.randomElement(tp);
				UtilParticle.PlayParticleToAll(ParticleType.WITCH_MAGIC, getEntity().getLocation(), null, 0, 2, ViewDist.MAX);
				UtilParticle.PlayParticleToAll(ParticleType.WITCH_MAGIC, to, null, 0, 2, ViewDist.MAX);
				getEntity().teleport(to);
			}
		}
	}
}