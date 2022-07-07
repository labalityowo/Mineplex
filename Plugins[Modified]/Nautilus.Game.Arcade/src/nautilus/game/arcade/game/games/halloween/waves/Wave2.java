package nautilus.game.arcade.game.games.halloween.waves;

import mineplex.core.common.util.UtilTime;
import nautilus.game.arcade.game.games.halloween.Halloween;
import nautilus.game.arcade.game.games.halloween.HalloweenAudio;
import nautilus.game.arcade.game.games.halloween.creatures.MobCreeper;
import nautilus.game.arcade.game.games.halloween.creatures.MobGiant;
import nautilus.game.arcade.game.games.halloween.creatures.MobZombie;

public class Wave2 extends WaveBase
{
	public Wave2(Halloween host) 
	{
		super(host, "Giant Zombie is here to smash your brains!", 65000, host.GetSpawnSet(0), HalloweenAudio.WAVE_2);
	}

	@Override
	public void Spawn(int tick) 
	{
		if (UtilTime.elapsed(_start, 30000))
			return;
		
		if (tick == 0)
			Host.AddCreature(new MobGiant(Host, GetSpawn()));
		
		if (Host.GetCreatures().size() > Host.GetMaxMobs())
			return;
		
		if (tick % 10 == 0)
			Host.AddCreature(new MobZombie(Host, GetSpawn()));

		if (tick % 25 == 0)
			Host.AddCreature(new MobCreeper(Host, GetSpawn()));
	}
}
