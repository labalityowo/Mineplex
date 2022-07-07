package nautilus.game.arcade.game.games.halloween;

import org.bukkit.Location;

public class TargetData 
{
	public Location Target;
	public long Time;
	
	public TargetData(Location target)
	{
		SetTarget(target);
	}

	public void SetTarget(Location target) 
	{
		Target = target;
		Time = System.currentTimeMillis();
	}
}
