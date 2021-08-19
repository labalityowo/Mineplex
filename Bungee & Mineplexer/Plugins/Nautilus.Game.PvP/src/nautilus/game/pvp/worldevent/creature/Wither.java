package nautilus.game.pvp.worldevent.creature;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_6_R3.entity.CraftLivingEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;

import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import nautilus.game.pvp.worldevent.EventBase;
import nautilus.game.pvp.worldevent.EventMob;

public class Wither extends EventMob
{
	public Wither(EventBase event, Location location) 
	{
		super(event, location, "Charles Witherton", false, 800, EntityType.WITHER);
	}

	@Override
	public void Damaged(CustomDamageEvent event) 
	{
		//Null
	}
	
	@Override
	public void Spawn()
	{ 
		Event.Manager.Creature().SetForce(true);
		LivingEntity ent = (LivingEntity) GetLocation().getWorld().spawnEntity(GetLocation(), GetType());
		Event.Manager.Creature().SetForce(false);

		SetEntity(ent);
		
		if (GetEntity() instanceof CraftLivingEntity)
		{
			CraftLivingEntity craftEnt = (CraftLivingEntity)GetEntity();
			craftEnt.setCustomName(GetName()); 
			craftEnt.setCustomNameVisible(true);
		}
	}
	
	@Override
	public void Die()
	{
		Loot();
		Remove();
	}

	@Override
	public void Loot() 
	{
		Event.Manager.Loot().DropLoot(GetEntity().getEyeLocation(), 60, 60, 0.2f, 0.08f, 3d);
	}
}
