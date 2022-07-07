package nautilus.game.arcade.game.games.halloween.waves;

import java.util.List;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import nautilus.game.arcade.game.games.halloween.Halloween;
import nautilus.game.arcade.game.games.halloween.creatures.CreatureBase;

public class WaveVictory extends WaveBase
{
	public WaveVictory(Halloween host, List<Location> beaconSpawn) 
	{
		super(host, "Celebration!", 15000, beaconSpawn, null);
	}

	@Override
	public void Spawn(int tick) 
	{
		if (UtilTime.elapsed(_start, 20000))
			return;
		
		//Play
		if (tick == 0)
			for (Player player : UtilServer.getPlayers())
				player.playEffect(Host.WorldData.GetDataLocs("BLACK").get(0), Effect.RECORD_PLAY, 2259);
			
		//Mobs
		for (CreatureBase<? extends LivingEntity> mob : Host.GetCreatures())
			mob.GetEntity().damage(5);
		
		//Time
		if (Host.WorldTimeSet != 6000)
		{
			Host.WorldTimeSet = (Host.WorldTimeSet + 50)%24000;
			Host.WorldData.World.setTime(Host.WorldTimeSet);
		}
	}
}
