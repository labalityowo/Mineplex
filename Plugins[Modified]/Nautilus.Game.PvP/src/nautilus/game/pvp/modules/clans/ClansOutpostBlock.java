package nautilus.game.pvp.modules.clans;

import org.bukkit.Location;

public class ClansOutpostBlock 
{
	Location loc;
	int id;
	byte data;
	
	public ClansOutpostBlock(Location loc, int id, byte data)
	{
		this.loc = loc;
		this.id = id;
		this.data = data;
	}

	public void Build() 
	{
		loc.getBlock().setTypeIdAndData(id, data, true);
	}
}
