package mineplex.game.clans.clans.worldevent.raid.wither.creature.wither;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wither;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilParticle;
import mineplex.core.common.util.UtilParticle.ParticleType;
import mineplex.core.common.util.UtilParticle.ViewDist;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilShapes;
import mineplex.core.common.util.UtilTime;
import mineplex.game.clans.clans.worldevent.api.BossPassive;
import mineplex.game.clans.clans.worldevent.raid.wither.WitherRaid;

public class Decay extends BossPassive<CharlesWitherton, Wither>
{
	private static final double RANGE = 28;
	private long _lastUsed;
	private int _chargeTicks;
	
	public Decay(CharlesWitherton creature)
	{
		super(creature);
		_lastUsed = -1;
		_chargeTicks = -1;
	}
	
	@Override
	public int getCooldown()
	{
		return 300;
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
				for (Player player : UtilPlayer.getInRadius(getLocation(), RANGE).keySet())
				{
					getBoss().getEvent().getDamageManager().NewDamageEvent(player, getEntity(), null, DamageCause.WITHER, 1000, false, true, true, getEntity().getName(), "Decay");
				}
			}
			else
			{
				getEntity().teleport(getBoss().getCustomLocs("C_SEVEN_RWC").get(0));
				for (Location loc : UtilShapes.getSphereBlocks(getEntity().getEyeLocation(), 2, 2, true))
				{
					UtilParticle.playColoredParticleToAll(Color.RED, ParticleType.RED_DUST, loc, 2, ViewDist.MAX);
					UtilParticle.playColoredParticleToAll(Color.BLACK, ParticleType.RED_DUST, loc, 2, ViewDist.MAX);
				}
			}
			return;
		}
		if (getBoss().getHealthPercent() <= 0.5)
		{
			if (UtilTime.elapsed(_lastUsed, getCooldown() * 1000))
			{
				_chargeTicks = 0;
				((WitherRaid)getBoss().getEvent()).getPlayers().forEach(player -> UtilPlayer.message(player, F.main(getBoss().getEvent().getName() + " Raid", "He's charging up Decay! Run away!")));
			}
		}
	}
}