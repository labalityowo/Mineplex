package mineplex.game.clans.clans.worldevent.raid.wither.creature.magma;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.MagmaCube;

import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilTime;
import mineplex.game.clans.clans.worldevent.api.BossPassive;
import mineplex.game.clans.clans.worldevent.raid.wither.challenge.six.ChallengeSix;

public class MagmusCataclysm extends BossPassive<Magmus, MagmaCube>
{
	private long _lastUse;
	private List<Class<? extends Cataclysm>> _cataclysms = new ArrayList<>();
	
	public MagmusCataclysm(Magmus creature)
	{
		super(creature);
		_lastUse = System.currentTimeMillis() - (getCooldown() * 1000);
		_cataclysms.add(HeatingUp.class);
		_cataclysms.add(InfernalMinions.class);
		_cataclysms.add(UndeadAlly.class);
	}
	
	@Override
	public int getCooldown()
	{
		return 23;
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
		if (UtilTime.elapsed(_lastUse, getCooldown() * 1000))
		{
			_lastUse = System.currentTimeMillis();
			if (!_cataclysms.isEmpty())
			{
				try
				{
					UtilMath.randomElement(_cataclysms).getConstructor(ChallengeSix.class, Magmus.class).newInstance(getBoss().getChallenge(), getBoss());
				}
				catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e)
				{
					e.printStackTrace();
				}
			}
		}
	}
}