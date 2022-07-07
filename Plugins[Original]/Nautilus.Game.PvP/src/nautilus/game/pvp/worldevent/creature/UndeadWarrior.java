package nautilus.game.pvp.worldevent.creature;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_6_R3.entity.CraftZombie;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;

import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilMath;
import nautilus.game.pvp.worldevent.EventBase;
import nautilus.game.pvp.worldevent.EventMob;
import net.minecraft.server.v1_6_R3.EntityZombie;
import net.minecraft.server.v1_6_R3.Item;
import net.minecraft.server.v1_6_R3.ItemStack;

public class UndeadWarrior extends EventMob
{
	public UndeadWarrior(EventBase event, Location location) 
	{
		super(event, location, "Undead Warrior", true, 60, EntityType.ZOMBIE);
	}

	@Override
	public void SpawnCustom() 
	{
		if (!(GetEntity() instanceof Zombie))
			return;

		try
		{
			Zombie zomb = (Zombie)GetEntity();
			CraftZombie zombC = (CraftZombie)zomb;
			EntityZombie zombMC = zombC.getHandle();

			zombMC.setEquipment(0, new ItemStack(Item.IRON_SWORD));
			zombMC.setEquipment(1, new ItemStack(Item.IRON_BOOTS));
			zombMC.setEquipment(2, new ItemStack(Item.IRON_LEGGINGS));
			zombMC.setEquipment(3, new ItemStack(Item.IRON_CHESTPLATE));
			zombMC.setEquipment(4, new ItemStack(Item.IRON_HELMET));
		}
		catch (Exception e)
		{
			System.out.println("Zombie Armor Error.");
		}
	}

	@EventHandler
	public void Leap(UpdateEvent event)
	{
		if (GetEntity() == null)
			return;

		if (event.getType() != UpdateType.FAST)
			return;
		
		if (Math.random() < 0.9)
			return;

		if (!(GetEntity() instanceof Zombie))
			return;
		
		Zombie zombie = (Zombie)GetEntity();
		
		if (zombie.getTarget() == null)
			return;
		
		double dist = UtilMath.offset(zombie.getTarget(), zombie);
		
		if (dist <= 3 || dist > 16)
			return;
		
		double power = 0.6 + (1.2 * ((dist-3)/13d));
		
		//Leap
		UtilAction.velocity(zombie, UtilAlg.getTrajectory(zombie, zombie.getTarget()), 
				power, false, 0, 0.4, 1, true);
		
		//Effect
		zombie.getWorld().playSound(zombie.getLocation(), Sound.ZOMBIE_HURT, 1f, 2f);
	}
	
	@EventHandler
	public void Conditions(UpdateEvent event)
	{
		if (GetEntity() == null)
			return;

		if (event.getType() != UpdateType.SEC)
			return;
		
		ModifyHealth(1);
		Event.Manager.Condition().Factory().Speed("Undead Haste", GetEntity(), GetEntity(), 1.9, 2, false, false);
	}
	
	@Override
	public void Die()
	{
		Event.Manager.Blood().Effects(GetEntity().getEyeLocation(), 10, 0.5, 
				Sound.ZOMBIE_DEATH, 1f, 1f, Material.ROTTEN_FLESH, (byte)0, true);
		Loot();
		Remove();
	}

	@Override
	public void Loot() 
	{
		if (Math.random() > 0.97)
			GetEntity().getWorld().dropItem(GetEntity().getLocation(), new org.bukkit.inventory.ItemStack(Material.IRON_HELMET));
		
		if (Math.random() > 0.97)
			GetEntity().getWorld().dropItem(GetEntity().getLocation(), new org.bukkit.inventory.ItemStack(Material.IRON_CHESTPLATE));
		
		if (Math.random() > 0.97)
			GetEntity().getWorld().dropItem(GetEntity().getLocation(), new org.bukkit.inventory.ItemStack(Material.IRON_LEGGINGS));
		
		if (Math.random() > 0.97)
			GetEntity().getWorld().dropItem(GetEntity().getLocation(), new org.bukkit.inventory.ItemStack(Material.IRON_BOOTS));
		
		if (Math.random() > 0.90)
			GetEntity().getWorld().dropItem(GetEntity().getLocation(), new org.bukkit.inventory.ItemStack(Material.IRON_SWORD));
		
		for (int i=0 ; i<UtilMath.r(5) + 1 ; i++)
			GetEntity().getWorld().dropItem(GetEntity().getLocation(), new org.bukkit.inventory.ItemStack(Material.EMERALD));
	}
}
