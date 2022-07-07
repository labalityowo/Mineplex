package mineplex.game.clans.items.legendaries;

import org.bukkit.Material;
import org.bukkit.entity.Player;

public class MeteorBow extends LegendaryItem
{
	public static final int MAX_FLIGHT_TIME = 80;	// Max flight of 80 ticks
	
	private long _flightTime;	// Time (in ticks) since last touching ground and flying
	
	public MeteorBow()
	{
		super("Meteor Bow", new String[]
		{
			"Shoot explosive arrows!"
		}, Material.BOW);
		_flightTime = 0;
	}
	
	@Override
	public void update(Player wielder)
	{
		if (isHoldingRightClick() && canPropel())
		{
			propelPlayer(wielder);
		}
	}

	private void propelPlayer(Player player)
	{
		_flightTime++;
		// TODO: Propel player forward with ??? velocity
	}
	
	private boolean canPropel()
	{
		return _flightTime <= MAX_FLIGHT_TIME;
	}
}