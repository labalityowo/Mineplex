package mineplex.game.clans.clans.worldevent.raid.wither.creature.wither;

import org.bukkit.entity.Wither;

import mineplex.core.common.util.UtilTime;
import mineplex.game.clans.clans.worldevent.api.BossPassive;

public class Flight extends BossPassive<CharlesWitherton, Wither>
{
	private long _lastUsed;
	private int _flyingTicks;
	
	public Flight(CharlesWitherton creature)
	{
		super(creature);
		_lastUsed = -1;
		_flyingTicks = -1;
	}
	
	@Override
	public int getCooldown()
	{
		return 200;
	}
	
	@Override
	public boolean isProgressing()
	{
		return _flyingTicks != -1;
	}

	@Override
	public void tick()
	{
		if (_flyingTicks != -1)
		{
			_flyingTicks++;
			if (_flyingTicks >= (20 * 20))
			{
				_lastUsed = System.currentTimeMillis();
				_flyingTicks = -1;
				getBoss().Flying = false;
			}
			return;
		}
		if (getBoss().getHealthPercent() <= 0.75)
		{
			if (_lastUsed == -1)
			{
				_lastUsed = System.currentTimeMillis();
			}
			else
			{
				if (UtilTime.elapsed(_lastUsed, getCooldown() * 1000))
				{
					getBoss().Flying = true;
					_flyingTicks = 0;
				}
			}
		}
	}
}