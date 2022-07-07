package nautilus.game.arcade.game.games.halloween2016.wave;

import mineplex.core.common.util.UtilTime;

import nautilus.game.arcade.game.games.halloween2016.Halloween2016;
import nautilus.game.arcade.game.games.halloween2016.creatures.MobSkeletonArcher;
import nautilus.game.arcade.game.games.halloween2016.creatures.MobZombie;

public class Wave1 extends WaveBase
{

	public Wave1(Halloween2016 host)
	{
		super(host, null, 80000, host.getMobSpawns(), null);
		_desc = new String[]
				{
						"Zombies", 
						"Skeletons"
				};
	}

	@Override
	public void Spawn(int tick)
	{
		if(tick == 0)
		{
			Host.setObjective("Protect the Crypt");
		}
		
		if (UtilTime.elapsed(_start, 50000))
			return;
		
		if (Host.getNonPumplings().size() > Host.getMaxNonPumplings()) return;
		
		if(tick%10 == 0)
		{
			Host.AddCreature(new MobZombie(Host, GetSpawn()));
		}
		
		if(tick%15 == 0)
		{
			Host.AddCreature(new MobSkeletonArcher(Host, GetSpawn()));
		}
	}

}
