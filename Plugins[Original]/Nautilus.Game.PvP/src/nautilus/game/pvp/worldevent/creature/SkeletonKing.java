package nautilus.game.pvp.worldevent.creature;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftSkeleton;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Skeleton;
import org.bukkit.entity.Skeleton.SkeletonType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.common.util.UtilTime;
import nautilus.game.pvp.worldevent.EventBase;
import nautilus.game.pvp.worldevent.EventMobBoss;
import nautilus.game.pvp.worldevent.EventMobMinion;
import net.minecraft.server.v1_7_R4.EntitySkeleton;
import net.minecraft.server.v1_7_R4.Item;
import net.minecraft.server.v1_7_R4.ItemStack;

public class SkeletonKing extends EventMobBoss
{
	private int _minionsMax = 16;
	
	private long _stateTime = System.currentTimeMillis();
	
	public SkeletonKing(EventBase event, Location location) 
	{
		super(event, location, "Skeleton King", true, 800, EntityType.SKELETON);
		
		_minionTargetLimit = 4;
	}

	@Override
	public void SpawnCustom() 
	{
		if (!(GetEntity() instanceof Skeleton))
			return;

		try
		{
			Skeleton skel = (Skeleton)GetEntity();
			skel.setSkeletonType(SkeletonType.WITHER);
			CraftSkeleton skelC = (CraftSkeleton)skel;
			EntitySkeleton skelMC = skelC.getHandle();

			skelMC.setEquipment(0, new ItemStack(Item.getById(Material.IRON_SWORD.getId())));
		}
		catch (Exception e)
		{
			System.out.println("Skeleton Armor Error.");
		}
	}
	
	@Override
	public void DamagedCustom(CustomDamageEvent event)
	{
		if (event.GetCause() == DamageCause.PROJECTILE)
		{
			if (!event.GetDamageeEntity().equals(GetEntity()))
				return;
			
			event.SetCancelled("Skeleton Resistance");
		}
		
		if (event.GetCause() == DamageCause.FIRE)
		{
			if (!event.GetDamageeEntity().equals(GetEntity()))
				return;
			
			GetEntity().setFireTicks(0);
			
			event.SetCancelled("Skeleton Resistance");
		}
	}
	
	@EventHandler
	public void Combust(EntityCombustEvent event)
	{
		if (GetEntity() == null)
			return;
		
		if (event.getEntity().equals(GetEntity()))
			event.setCancelled(true);
	}
		
	@EventHandler
	public void Heal(UpdateEvent event)
	{
		if (GetEntity() == null)
			return;

		if (event.getType() != UpdateType.SEC)
			return;
		
		ModifyHealth(1);
	}
	
	@Override
	public void Die()
	{
		Event.Manager.Blood().Effects(GetEntity().getEyeLocation(), 50, 0.8, 
				Sound.SKELETON_DEATH, 2f, 0.2f, Material.BONE, (byte)0, false);
		Loot();
		Remove();
	}

	@Override
	public void Loot() 
	{
		Event.Manager.Loot().DropLoot(GetEntity().getEyeLocation(), 40, 40, 0.2f, 0.05f, 3d);
	}
	
	@EventHandler
	public void MinionSpawn(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
			return;
		
		if (GetState() != 1)
			return;
		
		if (GetEntity() == null)
			return;
		
		if (_minions.size() >= _minionsMax)
			return;
		
		Event.CreatureRegister(new SkeletonMinion(Event, GetEntity().getLocation(), this));
		
		MinionPositions();
	}
	
	public void MinionPositions()
	{
		int i = 0;
		for (EventMobMinion cur : _minions)
		{
			cur.SetRadialLead(i * ((2d * Math.PI)/_minions.size()));
			i++;
		}	
	}

	public int GetState() 
	{
		return _state;
	}
	
	public void SetState(int state)
	{
		_state = state;
		_stateTime = System.currentTimeMillis();
		
		for (EventMobMinion cur : _minions)
			cur.StateChange(state);
	}
	
	@EventHandler
	public void StateChanger(UpdateEvent event)
	{
		if (GetEntity() == null)
			return;

		if (event.getType() != UpdateType.FAST)
			return;
		
		if (GetState() == 0)
		{
			if (UtilTime.elapsed(_stateTime, 30000))
			{
				SetState(1);
				((Skeleton)GetEntity()).setTarget(null);
			}
		}
		else if (GetState() == 1)
		{
			if (UtilTime.elapsed(_stateTime, 8000) && _minions.size() == _minionsMax)
			{
				SetState(2);
			}
		}
		else if (GetState() == 2)
		{
			if (UtilTime.elapsed(_stateTime, 8000))
			{
				SetState(0);
			}
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void TargetCancel(EntityTargetEvent event)
	{
		if (GetState() == 1 && event.getEntity().equals(GetEntity()))
			event.setCancelled(true);
	}

	@Override
	public void DistanceAction() 
	{
		// TODO Auto-generated method stub
		
	}
}
