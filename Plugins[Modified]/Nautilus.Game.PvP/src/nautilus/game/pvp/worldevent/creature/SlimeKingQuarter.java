package nautilus.game.pvp.worldevent.creature;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Slime;
import org.bukkit.util.Vector;

import nautilus.game.pvp.worldevent.EventBase;

public class SlimeKingQuarter extends SlimeBase
{
	public SlimeKingQuarter(EventBase event, Location location) 
	{
		super(event, location, "1/4 of Slime King", 150);
		
		loot = 10;
		speed = 5;
		rocketCount = 1;
		shieldCount = 2;
	}

	@Override
	public void SpawnCustom() 
	{
		if (!(GetEntity() instanceof Slime))
			return;

		Slime slime = (Slime)GetEntity();
		slime.setSize(4);
	}
	
	@Override
	public void Split()
	{
		for (int i=0 ; i<24 ; i++)
		{
			Slime slime = (Slime) GetEntity().getWorld().spawnEntity(GetEntity().getLocation(), EntityType.SLIME);
			slime.setSize(1);
			slime.setVelocity(new Vector(Math.random()-0.5, Math.random()/2, Math.random()-0.5));
			
			Event.Manager.Condition().Factory().Speed("Slime Speed", slime, slime, 60, 7, false, false);
		}
	}
}
