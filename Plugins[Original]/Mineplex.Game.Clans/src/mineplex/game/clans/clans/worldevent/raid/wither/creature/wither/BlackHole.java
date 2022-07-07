package mineplex.game.clans.clans.worldevent.raid.wither.creature.wither;

import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.util.Vector;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.game.clans.clans.worldevent.api.BossPassive;
import mineplex.game.clans.clans.worldevent.raid.wither.WitherRaid;

public class BlackHole extends BossPassive<CharlesWitherton, Wither>
{
	private static final double RANGE = 15;
	private long _lastUsed;
	private int _chargeTicks;
	
	public BlackHole(CharlesWitherton creature)
	{
		super(creature);
		_lastUsed = -1;
		_chargeTicks = -1;
	}
	
	private void pull()
	{
		for (Player entity : UtilPlayer.getInRadius(getLocation(), RANGE).keySet())
		{
			UtilAction.velocity(entity, UtilAlg.getTrajectory(entity, getEntity()), 0.3, false, 0, 0, 1, true);
		}
        for (int i = 0; i < 6; i++)
        {
			Vector random = new Vector(Math.random() * 4 - 2, Math.random() * 4 - 2, Math.random() * 4 - 2);

			Location origin = getLocation().add(0, 1.3, 0);
			origin.add(getLocation().getDirection().multiply(10));
			origin.add(random);

			Vector vel = UtilAlg.getTrajectory(origin, getLocation().add(0, 1.3, 0));
			vel.multiply(7);

			UtilParticle.PlayParticle(ParticleType.MAGIC_CRIT,
							origin,
							(float)vel.getX(),
							(float)vel.getY(),
							(float)vel.getZ(),
							1, 0, ViewDist.LONG, UtilServer.getPlayers());
        }
	}
	
	private void throwUp()
	{
		Map<Player, Double> near = UtilPlayer.getInRadius(getLocation(), RANGE);
		for (Entry<Player, Double> thr : near.entrySet())
		{
			Vector vel = new Vector(0, Math.min(7.5, Math.max(5 / thr.getValue(), 4)), 0);
			thr.getKey().setVelocity(vel);
		}
	}
	
	@Override
	public int getCooldown()
	{
		return 60;
	}
	
	@Override
	public boolean isProgressing()
	{
		return _chargeTicks != -1;
	}

	@Override
	public void tick()
	{
		if (_chargeTicks != -1)
		{
			_chargeTicks++;
			if (_chargeTicks >= (20 * 5))
			{
				_lastUsed = System.currentTimeMillis();
				_chargeTicks = -1;
				throwUp();
			}
			else
			{
				pull();
			}
			return;
		}
		if (getBoss().getHealthPercent() <= 0.25)
		{
			if (_lastUsed == -1)
			{
				_lastUsed = System.currentTimeMillis();
			}
			else
			{
				if (UtilTime.elapsed(_lastUsed, getCooldown() * 1000))
				{
					_chargeTicks = 0;
					((WitherRaid)getBoss().getEvent()).getPlayers().forEach(player -> UtilPlayer.message(player, F.main(getBoss().getEvent().getName() + " Raid", "He's charging up Decay! Run away!")));
				}
			}
		}
	}
	
	@EventHandler
	public void onKick(PlayerKickEvent event)
	{
		if (_chargeTicks != -1 && getBoss().getChallenge().getRaid().getPlayers().contains(event.getPlayer()))
		{
			event.setCancelled(true);
		}
	}
}