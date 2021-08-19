package nautilus.game.arcade.game.games.halloween2016.wave;

import mineplex.core.common.util.UtilTime;

import nautilus.game.arcade.game.games.halloween.creatures.MobSpiderLeaper;
import nautilus.game.arcade.game.games.halloween.creatures.MobSpiderSmasher;
import nautilus.game.arcade.game.games.halloween2016.Halloween2016;
import nautilus.game.arcade.game.games.halloween2016.creatures.MobBlaze;
import nautilus.game.arcade.game.games.halloween2016.creatures.MobGiant;
import nautilus.game.arcade.game.games.halloween2016.creatures.MobSkeletonArcher;
import nautilus.game.arcade.game.games.halloween2016.creatures.MobWitch;
import nautilus.game.arcade.game.games.halloween2016.creatures.MobZombie;
import nautilus.game.arcade.game.games.halloween2016.creatures.MobZombieSpawner;

public class Wave5 extends WaveBase
{

	public Wave5(Halloween2016 host)
	{
		super(host, null, 120000, host.getMobSpawns(), null);
		_desc = new String[]
				{
					"Giants",
					"Creepers",
					"Zombies",
					"Zombie Spawners",
					"Skeletons",
					"Witches",
					"Blazes",
					"Spiders"
				};
	}

	@Override
	public void Spawn(int tick)
	{
		if(tick%(20*30) == 0)
		{
			Host.AddCreature(new MobGiant(Host, getSpawn(Host.getMainLane())));
		}
		
		if (UtilTime.elapsed(_start, 100000))
			return;
		
		if(tick%300 == 0)
		{
			spawnCreepers();
		}
		
		if (Host.getNonPumplings().size() > Host.getMaxNonPumplings()) return;
		
		if(tick%40 == 0)
		{
			Host.AddCreature(new MobZombie(Host, GetSpawn()));
		}
		
		if(tick%40 == 0)
		{
			Host.AddCreature(new MobSkeletonArcher(Host, GetSpawn()));
		}
		
		if(tick%50 == 0)
		{
			Host.AddCreature(new MobZombieSpawner(Host, GetSpawn()));
		}
		
		if(tick%50 == 0)
		{
			Host.AddCreature(new MobWitch(Host, GetSpawn()));
		}
		
		if(tick%50 == 0)
		{
			Host.AddCreature(new MobBlaze(Host, GetSpawn()));
		}
		
		if(tick%40 == 0)
		{
			Host.AddCreature(new MobSpiderLeaper(Host, GetSpawn()));
			Host.AddCreature(new MobSpiderSmasher(Host, GetSpawn()));
		}
	}

}
