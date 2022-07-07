package nautilus.game.arcade.game.games.halloween2016.wave;

import mineplex.core.common.util.UtilTime;
import nautilus.game.arcade.game.games.halloween2016.Halloween2016;
import nautilus.game.arcade.game.games.halloween2016.creatures.MobBlaze;
import nautilus.game.arcade.game.games.halloween2016.creatures.MobGiant;
import nautilus.game.arcade.game.games.halloween2016.creatures.MobPigZombie;
import nautilus.game.arcade.game.games.halloween2016.creatures.MobWitch;

public class Wave4 extends WaveBase
{

	public Wave4(Halloween2016 host)
	{
		super(host, null, 100000, host.getMobSpawns(), null);
		_desc = new String[]
				{
						"Giants",
						"Creepers",
						"Zombie Pigmen",
						"Blazes",
						"Witches",
				};
	}

	@Override
	public void Spawn(int tick)
	{
		if(tick%(20 * 30) == 0)
		{
			Host.AddCreature(new MobGiant(Host, getSpawn(Host.getMainLane())));
		}
		
		if (Host.getNonPumplings().size() > Host.getMaxNonPumplings()) return;
		
		if (UtilTime.elapsed(_start, 70000))
			return;
		
		if(tick%15 == 0)
		{
			Host.AddCreature(new MobPigZombie(Host, GetSpawn()));
		}
		
		if(tick%60 == 0)
		{
			Host.AddCreature(new MobBlaze(Host, GetSpawn()));
		}
		
		if(tick%100 == 0)
		{
			Host.AddCreature(new MobWitch(Host, GetSpawn()));
		}
		
	}

}
