package nautilus.game.arcade.game.games.halloween.waves;

import org.bukkit.Location;

import mineplex.core.common.util.UtilTime;
import nautilus.game.arcade.game.games.halloween.Halloween;
import nautilus.game.arcade.game.games.halloween.HalloweenAudio;
import nautilus.game.arcade.game.games.halloween.creatures.MobGhast;
import nautilus.game.arcade.game.games.halloween.creatures.MobPigZombie;

public class Wave4 extends WaveBase
{
	public Wave4(Halloween host) 
	{
		super(host, "Look up! Its the Ghasts and Ghouls!", 80000, host.GetSpawnSet(3), HalloweenAudio.WAVE_4);
	}

	@Override
	public void Spawn(int tick) 
	{
		if (UtilTime.elapsed(_start, 30000))
			return;
		
		if (tick > 0 && tick % 70 == 0)
		{
			Location loc = GetSpawn().clone();
			loc.setY(30 + 20 * Math.random());
			loc.setX(80 * Math.random() - 40);
			loc.setZ(80 * Math.random() - 40);
			Host.AddCreature(new MobGhast(Host, loc));
		}
			
		if (Host.GetCreatures().size() > Host.GetMaxMobs())
			return;
		
		if (tick % 10 == 0)
			Host.AddCreature(new MobPigZombie(Host, GetSpawn()));
	}
}
