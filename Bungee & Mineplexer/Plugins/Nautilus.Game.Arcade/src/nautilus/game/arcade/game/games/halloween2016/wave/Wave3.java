package nautilus.game.arcade.game.games.halloween2016.wave;

import mineplex.core.common.util.UtilTime;

import nautilus.game.arcade.game.games.halloween2016.Halloween2016;
import nautilus.game.arcade.game.games.halloween2016.creatures.MobBlaze;
import nautilus.game.arcade.game.games.halloween2016.creatures.MobGiant;
import nautilus.game.arcade.game.games.halloween2016.creatures.MobWitch;
import nautilus.game.arcade.game.games.halloween2016.creatures.MobZombie;
import nautilus.game.arcade.game.games.halloween2016.creatures.MobZombieSpawner;

public class Wave3 extends WaveBase
{

	public Wave3(Halloween2016 host)
	{
		super(host, null, 90000, host.getMobSpawns(), null);
		_desc = new String[]
				{
					"Giants",
					"Creepers",
					"Zombies",
					"Zombie Spawners",
					"Blazes"
				};
	}

	@Override
	public void Spawn(int tick)
	{
		if (UtilTime.elapsed(_start, 60000))
			return;
		
		if(tick%200 == 0)
		{
			spawnCreepers();
		}
		
		if(tick%(20*7) == 0)
		{
			Host.AddCreature(new MobWitch(Host, GetSpawn()));
		}
		
		if(tick%(20 * 10) == 0)
		{
			Host.AddCreature(new MobGiant(Host, getSpawn(Host.getMainLane())));
		}
		
		if (Host.getNonPumplings().size() > Host.getMaxNonPumplings()) return;
		
		if(tick%15 == 0)
		{
			Host.AddCreature(new MobZombie(Host, GetSpawn()));
		}
		
		if(tick%60 == 0)
		{
			Host.AddCreature(new MobZombieSpawner(Host, GetSpawn()));
		}
		
		if(tick%30 == 0)
		{
			Host.AddCreature(new MobBlaze(Host, GetSpawn()));
		}
		
	}

}
