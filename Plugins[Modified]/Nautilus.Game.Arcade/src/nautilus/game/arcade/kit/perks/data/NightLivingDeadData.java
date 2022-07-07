package nautilus.game.arcade.kit.perks.data;

import java.util.ArrayList;

import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;

public class NightLivingDeadData
{
	public Player Player;
	public long Time;
	
	public long LastSpawn = 0;
	public ArrayList<Zombie> Zombies = new ArrayList<Zombie>();
	
	public NightLivingDeadData(Player player)
	{
		Player = player;
		Time = System.currentTimeMillis();
	}
}
