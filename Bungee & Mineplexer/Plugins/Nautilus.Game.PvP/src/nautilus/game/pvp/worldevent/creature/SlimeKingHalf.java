package nautilus.game.pvp.worldevent.creature;

import org.bukkit.Location;
import org.bukkit.entity.Slime;

import nautilus.game.pvp.worldevent.EventBase;

public class SlimeKingHalf extends SlimeBase
{
	public SlimeKingHalf(EventBase event, Location location) 
	{
		super(event, location, "1/2 of Slime King", 300);
		
		loot = 15;
		speed = 3;
		rocketCount = 2;
		shieldCount = 4;
	}

	@Override
	public void SpawnCustom() 
	{
		if (!(GetEntity() instanceof Slime))
			return;

		Slime slime = (Slime)GetEntity();
		slime.setSize(6);
	}

	@Override
	public void Split()
	{
		Event.CreatureRegister(new SlimeKingQuarter(Event, GetEntity().getLocation()));
		Event.CreatureRegister(new SlimeKingQuarter(Event, GetEntity().getLocation()));
	}
}
