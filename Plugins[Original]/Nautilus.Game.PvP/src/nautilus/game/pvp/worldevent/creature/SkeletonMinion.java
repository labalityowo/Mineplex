package nautilus.game.pvp.worldevent.creature;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftCreature;
import org.bukkit.craftbukkit.v1_7_R4.entity.CraftSkeleton;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;

import mineplex.minecraft.game.core.damage.CustomDamageEvent;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.updater.UpdateType;
import nautilus.game.pvp.worldevent.EventBase;
import nautilus.game.pvp.worldevent.EventMobMinion;
import net.minecraft.server.v1_7_R4.EntityCreature;
import net.minecraft.server.v1_7_R4.EntitySkeleton;
import net.minecraft.server.v1_7_R4.Item;
import net.minecraft.server.v1_7_R4.ItemStack;
import net.minecraft.server.v1_7_R4.Navigation;

public class SkeletonMinion extends EventMobMinion
{
	public SkeletonMinion(EventBase event, Location location, SkeletonKing host) 
	{
		super(event, location, "Skeleton Minion", true, 32, EntityType.SKELETON, host);

		if (GetState() == 0)
			SetWeapon(Item.getById(Material.IRON_SWORD.getId()));
	}

	@Override
	public void SpawnCustom() 
	{
		if (!(GetEntity() instanceof Skeleton))
			return;

		try
		{
			Skeleton skel = (Skeleton)GetEntity();
			CraftSkeleton skelC = (CraftSkeleton)skel;
			EntitySkeleton skelMC = skelC.getHandle();

			skelMC.setEquipment(0, new ItemStack(Item.getById(Material.BOW.getId())));
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
		
		if (Math.random() > 0.5)
		{
			LivingEntity damager = event.GetDamagerEntity(true);
			if (damager != null)
				SetTarget(damager);
		}
	}
	
	@Override
	public void StateChange(int newState)
	{
		if (newState == 0)
			SetWeapon(Item.IRON_SWORD);
		
		if (newState == 1)
			SetWeapon(Item.BOW);
	}
	
	public void SetWeapon(Item item)
	{
		((Skeleton)GetEntity()).setTarget(null);

		try
		{
			Skeleton skel = (Skeleton)GetEntity();
			CraftSkeleton skelC = (CraftSkeleton)skel;
			EntitySkeleton skelMC = skelC.getHandle();

			skelMC.setEquipment(0, new ItemStack(item));
		}
		catch (Exception e)
		{
			System.out.println("Skeleton Weapon Error.");
		}
	}
	
	@Override
	public boolean TargetCancelCustom(Entity target)
	{
		return (GetState() != 0 && !target.equals(GetEntity()));
	}

	@Override
	public void Die()
	{
		Event.Manager.Blood().Effects(GetEntity().getEyeLocation(), 10, 0.5, 
				Sound.SKELETON_DEATH, 1f, 1f, Material.BONE, (byte)0, false);
		
		Loot();
		Remove();
	}

	@EventHandler(priority = EventPriority.HIGH) // AFTER ARROW
	public void Orbit(UpdateEvent event)
	{
		if (event.getType() != UpdateType.TICK)
			return;
		
		if (GetState() == 0)
			return;
		
		if (!Valid())
			return;
		
		double sizeMod = 2 + (((SkeletonKing)_host).GetMinions().size() / 20);

		//Orbit
		double speed = 20d;
		double oX = Math.sin(_host.GetEntity().getTicksLived()/speed + _radialLead) * 2 * sizeMod;
		double oY = 1;
		double oZ = Math.cos(_host.GetEntity().getTicksLived()/speed + _radialLead) * 2 * sizeMod;

		//Move
		EntityCreature ec = ((CraftCreature)GetEntity()).getHandle();
		Navigation nav = ec.getNavigation();
		Location loc = _host.GetEntity().getLocation().add(oX, oY, oZ);
		nav.a(loc.getX(), loc.getY(), loc.getZ(), 0.4f);
	}

	@EventHandler
	public void Arrow(UpdateEvent event)
	{
		if (GetState() != 2)
			return;

		if (event.getType() != UpdateType.FASTEST)
			return;

		if (!Valid())
			return;

		Arrow arrow = GetEntity().getWorld().spawnArrow(
				GetEntity().getEyeLocation().add(UtilAlg.getTrajectory2d(_host.GetEntity(), GetEntity())),
				UtilAlg.getTrajectory2d(_host.GetEntity(), GetEntity()).normalize(), 2f, 16f);
		arrow.setShooter(GetEntity());

		Event.AddArrow(arrow);
	}

	public int GetState()
	{
		return _host.GetState();
	}
}
