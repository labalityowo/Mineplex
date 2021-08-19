package nautilus.game.pvp.worldevent.creature;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_6_R3.entity.CraftSkeleton;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Skeleton;
import org.bukkit.event.EventHandler;

import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.common.util.UtilMath;
import nautilus.game.pvp.worldevent.EventBase;
import nautilus.game.pvp.worldevent.EventMob;
import net.minecraft.server.v1_6_R3.EntitySkeleton;
import net.minecraft.server.v1_6_R3.Item;
import net.minecraft.server.v1_6_R3.ItemStack;

public class UndeadArcher extends EventMob
{
	public UndeadArcher(EventBase event, Location location) 
	{
		super(event, location, "Undead Archer", true, 60, EntityType.SKELETON);
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

			skelMC.setEquipment(0, new ItemStack(Item.BOW));
			skelMC.setEquipment(1, new ItemStack(Item.CHAINMAIL_BOOTS));
			skelMC.setEquipment(2, new ItemStack(Item.CHAINMAIL_LEGGINGS));
			skelMC.setEquipment(3, new ItemStack(Item.CHAINMAIL_CHESTPLATE));
			skelMC.setEquipment(4, new ItemStack(Item.CHAINMAIL_HELMET));
		}
		catch (Exception e)
		{
			System.out.println("Skeleton Armor Error.");
		}
	}
		
	@EventHandler
	public void Condition(UpdateEvent event)
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
		Event.Manager.Blood().Effects(GetEntity().getEyeLocation(), 10, 0.5, 
				Sound.SKELETON_DEATH, 1f, 1f, Material.BONE, (byte)0, false);
		Loot();
		Remove();
	}

	@Override
	public void Loot() 
	{
		if (Math.random() > 0.97)
			GetEntity().getWorld().dropItem(GetEntity().getLocation(), new org.bukkit.inventory.ItemStack(Material.CHAINMAIL_HELMET));
		
		if (Math.random() > 0.97)
			GetEntity().getWorld().dropItem(GetEntity().getLocation(), new org.bukkit.inventory.ItemStack(Material.CHAINMAIL_CHESTPLATE));
		
		if (Math.random() > 0.97)
			GetEntity().getWorld().dropItem(GetEntity().getLocation(), new org.bukkit.inventory.ItemStack(Material.CHAINMAIL_LEGGINGS));
		
		if (Math.random() > 0.97)
			GetEntity().getWorld().dropItem(GetEntity().getLocation(), new org.bukkit.inventory.ItemStack(Material.CHAINMAIL_BOOTS));
		
		if (Math.random() > 0.90)
			GetEntity().getWorld().dropItem(GetEntity().getLocation(), new org.bukkit.inventory.ItemStack(Material.BOW));
		
		GetEntity().getWorld().dropItem(GetEntity().getLocation(), new org.bukkit.inventory.ItemStack(Material.ARROW, UtilMath.r(12) + 1));
		
		for (int i=0 ; i<UtilMath.r(5) + 1 ; i++)
			GetEntity().getWorld().dropItem(GetEntity().getLocation(), new org.bukkit.inventory.ItemStack(Material.EMERALD));
	}
}
