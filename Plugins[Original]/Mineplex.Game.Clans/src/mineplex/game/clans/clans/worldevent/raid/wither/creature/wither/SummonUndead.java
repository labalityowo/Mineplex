package mineplex.game.clans.clans.worldevent.raid.wither.creature.wither;

import org.bukkit.entity.Wither;

import mineplex.core.common.util.UtilTime;
import mineplex.game.clans.clans.worldevent.api.BossPassive;
import mineplex.game.clans.clans.worldevent.raid.wither.creature.mage.UndeadKnight;

public class SummonUndead extends BossPassive<CharlesWitherton, Wither>
{
	private long _lastUsed;
	
	public SummonUndead(CharlesWitherton creature)
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
		if (getBoss().getHealthPercent() <= 0.50)
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
					for (int i = 0; i < 6; i++)
					{
						getBoss().getEvent().registerCreature(new UndeadKnight(getBoss().getChallenge(), getLocation()));
					}
				}
			}
		}
	}
}