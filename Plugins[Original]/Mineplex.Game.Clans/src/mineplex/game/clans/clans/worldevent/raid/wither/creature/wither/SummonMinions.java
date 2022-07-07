package mineplex.game.clans.clans.worldevent.raid.wither.creature.wither;

import org.bukkit.entity.Wither;

import mineplex.core.common.util.UtilTime;
import mineplex.game.clans.clans.worldevent.api.BossPassive;

public class SummonMinions extends BossPassive<CharlesWitherton, Wither>
{
	private long _lastUsed;
	
	public SummonMinions(CharlesWitherton creature)
	{
		super(creature);
		_lastUsed = -1;
	}
	
	@Override
	public int getCooldown()
	{
		return 60;
	}
	
	@Override
	public boolean isProgressing()
	{
		return false;
	}

	@Override
	public void tick()
	{
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
					_lastUsed = System.currentTimeMillis();
					for (int i = 0; i < 2; i++)
					{
						getBoss().getEvent().registerCreature(new MiniCharles(getBoss(), getLocation()));
					}
				}
			}
		}
	}
}