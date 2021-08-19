package nautilus.game.arcade.game.games.halloween2016.wave;

import java.util.List;

import org.bukkit.Location;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilMath;

import nautilus.game.arcade.game.games.halloween.NamedAudio;
import nautilus.game.arcade.game.games.halloween2016.Halloween2016;
import nautilus.game.arcade.game.games.halloween2016.creatures.MobCreeper;

public abstract class WaveBase extends nautilus.game.arcade.game.games.halloween.waves.WaveBase
{
	
	protected Halloween2016 Host;
	
	protected static final int CREEPER_SPAWN_PACK_SIZE = 5;

	public WaveBase(Halloween2016 host, String name, long duration, List<Location> spawns, NamedAudio audio)
	{
		super(host, name, duration, spawns, audio);
		Host = host;
		
		_titleColor = C.cRed;
	}
	
	public Location getSpawn(List<Location> lane)
	{
		return lane.get(UtilMath.r(lane.size()));
	}
	
	public void spawnCreepers()
	{
		Location loc = GetSpawn();
		for(int i = 0; i < CREEPER_SPAWN_PACK_SIZE; i++)
		{
			double x = UtilMath.random(-3, 3);
			double z = UtilMath.random(-3, 3);
			
			Host.AddCreature(new MobCreeper(Host, loc.clone().add(x, 0, z)), false);
		}
	}
	
}
