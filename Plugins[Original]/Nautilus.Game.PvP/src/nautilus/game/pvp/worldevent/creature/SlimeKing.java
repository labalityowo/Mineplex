package nautilus.game.pvp.worldevent.creature;

import org.bukkit.Location;
import org.bukkit.entity.Slime;

import nautilus.game.pvp.worldevent.EventBase;

public class SlimeKing extends SlimeBase
{
	public SlimeKing(EventBase event, Location location) 
	{
		super(event, location, "Slime King", 600);
		
		loot = 20;
		speed = 1;
		rocketCount = 3;
		shieldCount = 8;
	}

	@Override
	public void SpawnCustom() 
	{
		if (!(GetEntity() instanceof Slime))
			return;

		Slime slime = (Slime)GetEntity();
		slime.setSize(9);
	}
	
	@Override
	public void Split()
	{
		Event.CreatureRegister(new SlimeKingHalf(Event, GetEntity().getLocation()));
		Event.CreatureRegister(new SlimeKingHalf(Event, GetEntity().getLocation()));
	}
}
