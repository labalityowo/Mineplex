package nautilus.game.minekart.item.world_items_custom;

import org.bukkit.Location;
import org.bukkit.Material;

import nautilus.game.minekart.item.KartItemEntity;
import nautilus.game.minekart.item.KartItemManager;
import nautilus.game.minekart.kart.Kart;

public class Flame extends KartItemEntity
{
	public Flame(KartItemManager manager, Kart kart, Location loc)
	{
		super(manager, kart, loc, Material.FIRE, (byte)0);
		
		SetRadius(1);
		
		this.SetFired();
	}
	
	@Override
	public void CollideHandle(Kart kart)
	{
		if (!kart.IsInvulnerable(false))
		{
			int ticks = Math.min(100, kart.GetDriver().getFireTicks() + 20);
			kart.GetDriver().setFireTicks(ticks);
		}
	}
	
	@Override
	public boolean TickUpdate()
	{
		return System.currentTimeMillis() - this.GetFireTime() > 6000;
	}
}
