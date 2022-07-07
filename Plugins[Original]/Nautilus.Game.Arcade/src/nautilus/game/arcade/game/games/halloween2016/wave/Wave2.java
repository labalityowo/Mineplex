package nautilus.game.arcade.game.games.halloween2016.wave;

import mineplex.core.common.util.UtilTime;

import nautilus.game.arcade.game.games.halloween.creatures.MobSpiderLeaper;
import nautilus.game.arcade.game.games.halloween.creatures.MobSpiderSmasher;
import nautilus.game.arcade.game.games.halloween2016.Halloween2016;
import nautilus.game.arcade.game.games.halloween2016.creatures.MobGiant;
import nautilus.game.arcade.game.games.halloween2016.creatures.MobZombie;

public class Wave2 extends WaveBase
{

	public Wave2(Halloween2016 host)
	{
		super(host, null, 90000, host.getMobSpawns(), null);
		_desc = new String[]
				{
					"Giants",
					"Creepers",
					"Zombies",
					"Spiders"
				};
	}

	@Override
	public void Spawn(int tick)
	{
		if(tick == 0)
		{
			Host.AddCreature(new MobGiant(Host, getSpawn(Host.getMainLane())));
		}
		
		if (UtilTime.elapsed(_start, 60000))
			return;
		
		if(tick%200 == 0)
		{
			spawnCreepers();
		}
		
		if (Host.getNonPumplings().size() > Host.getMaxNonPumplings()) return;
		
		if(tick%7 == 0)
		{
			Host.AddCreature(new MobZombie(Host, GetSpawn()));
		}
		
		if(tick%20 == 0)
		{
			Host.AddCreature(new MobSpiderLeaper(Host, GetSpawn()));
			Host.AddCreature(new MobSpiderSmasher(Host, GetSpawn()));
		}
	}

}
