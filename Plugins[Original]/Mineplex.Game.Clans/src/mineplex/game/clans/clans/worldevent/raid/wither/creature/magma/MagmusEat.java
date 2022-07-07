package mineplex.game.clans.clans.worldevent.raid.wither.creature.magma;

import org.bukkit.entity.MagmaCube;
import org.bukkit.entity.Player;

import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.game.clans.clans.worldevent.api.BossPassive;

public class MagmusEat extends BossPassive<Magmus, MagmaCube>
{
	private long _lastUse;
	private Player _eating;
	private int _ticks;
	
	public MagmusEat(Magmus creature)
	{
		super(creature);
		_lastUse = System.currentTimeMillis();
	}
	
	private void spit()
	{
		double offset = UtilMath.offset(_eating, getEntity());
		//Velocity
		UtilAction.velocity(_eating, 
				UtilAlg.getTrajectory2d(getLocation().toVector(), _eating.getLocation().toVector()), 
				2 + 2 * offset, true, 0, 1.2 + 1.0 * offset, 3, true);

		getBoss().getEvent().getCondition().Factory().Falling("Spit Out", _eating, getEntity(), 10, false, true);
		
		_lastUse = System.currentTimeMillis();
		_eating = null;
		_ticks = -1;
	}
	
	private void eat()
	{
		if (_ticks < 20 * 10 && !getBoss().HeatingRoom)
		{
			_eating.setFireTicks(40);
			_eating.teleport(getEntity());
		}
		else
		{
			spit();
		}
		_ticks++;
	}
	
	private void initialEat(Player target)
	{
		_eating = target;
		_ticks = 0;
		getBoss().getEvent().getCondition().Factory().Silence("Eat", _eating, getEntity(), 10, true, true);
	}
	
	@Override
	public int getCooldown()
	{
		return 15;
	}
	
	@Override
	public boolean isProgressing()
	{
		return false;
	}

	@Override
	public void tick()
	{
		if (getBoss().HeatingRoom)
		{
			return;
		}
		if (_eating != null)
		{
			eat();
			return;
		}
		if (UtilTime.elapsed(_lastUse, getCooldown() * 1000))
		{
			Player target = UtilPlayer.getClosest(getLocation(), 7);
			if (target != null)
			{
				initialEat(target);
			}
		}
	}
}