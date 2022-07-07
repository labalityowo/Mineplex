package nautilus.game.arcade.game.games.christmas;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftFallingSand;
import org.bukkit.entity.Chicken;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Skeleton;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.LeatherArmorMeta;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilMath;

public class SleighPart 
{

	public Chicken Ent;
	public FallingBlock Block;
	public double OffsetX;
	public double OffsetZ;
	
	public int Rise;
	public int Id;
	public int Data;
	public Location Location;
	
	SleighPart(Sleigh sleigh, int rise, int id, int data, Location loc, double x, double z)
	{
		//Base
		Ent = loc.getWorld().spawn(loc.add(x, 0, z), Chicken.class);
		Ent.setBaby();
		Ent.setAgeLock(true);
		Ent.setRemoveWhenFarAway(false);
		
		UtilEnt.vegetate(Ent, true);
		UtilEnt.ghost(Ent, true, false);
		sleigh.Host.Manager.GetCondition().Factory().Invisible("Sleigh", Ent, null, Double.MAX_VALUE, 3, false, false, true);

		//Height
		Rise = rise;
		Id = id;
		Data = data;
		Location = loc;
		OffsetX = x;
		OffsetZ = z;

		addRise(sleigh);
	}

	public void RefreshBlocks() 
	{
		if (Ent == null)
			return;
		
		Entity ent = Ent;
		
		while (ent.getPassenger() != null)
		{
			ent = ent.getPassenger();
			
			if (ent instanceof FallingBlock)
				((CraftFallingSand)ent).getHandle().ticksLived = 1;
		}
	}

	public void SetPresent() 
	{
		if (Ent == null)
			return;
		
		Block = Ent.getWorld().spawnFallingBlock(Ent.getLocation().add(0, 1, 0), 35, (byte)UtilMath.r(15));
		
		Entity top = Ent;
		while (top.getPassenger() != null)
			top = top.getPassenger();
		
		top.setPassenger(Block);
	}

	public Entity AddSanta() 
	{
		if (Ent == null)
			return null;
		
		Skeleton skel = Ent.getWorld().spawn(Ent.getLocation().add(0, 1, 0), Skeleton.class);
		UtilEnt.vegetate(skel);
		UtilEnt.ghost(skel, true, false);

		ItemStack head = new ItemStack(Material.LEATHER_HELMET);
		LeatherArmorMeta meta = (LeatherArmorMeta)head.getItemMeta();

		meta.setColor(Color.RED);
		meta.spigot().setUnbreakable(true);
		head.setItemMeta(meta);
		skel.getEquipment().setHelmet(head);

		ItemStack chest = new ItemStack(Material.LEATHER_CHESTPLATE);
		meta = (LeatherArmorMeta)chest.getItemMeta();
		meta.setColor(Color.RED);
		chest.setItemMeta(meta);
		skel.getEquipment().setChestplate(chest);

		ItemStack legs = new ItemStack(Material.LEATHER_LEGGINGS);
		meta = (LeatherArmorMeta)legs.getItemMeta();
		meta.setColor(Color.RED);
		legs.setItemMeta(meta);
		skel.getEquipment().setLeggings(legs);

		ItemStack boots = new ItemStack(Material.LEATHER_BOOTS);
		meta = (LeatherArmorMeta)boots.getItemMeta();
		meta.setColor(Color.BLACK);
		boots.setItemMeta(meta);
		skel.getEquipment().setBoots(boots);
		
		Entity top = Ent;
		while (top.getPassenger() != null)
			top = top.getPassenger();
		
		top.setPassenger(skel);

		skel.setCustomName(C.Bold + "Santa Claus");
		skel.setCustomNameVisible(true);
		
		skel.setRemoveWhenFarAway(false);
		
		return skel;
	}

	public boolean HasEntity(LivingEntity ent) 
	{
		if (Ent.equals(ent))
			return true;
		
		Entity top = Ent;
		
		while (top.getPassenger() != null)
		{
			top = top.getPassenger();
			
			if (top.equals(ent))
				return true;
		}
		
		return false;
	}
	
	private void addRise(Sleigh sleigh)
	{
		Chicken top = Ent;
		for (int i=0 ; i<Rise ; i++)
		{
			Chicken newTop = Location.getWorld().spawn(Ent.getLocation(), Chicken.class);
			newTop.setBaby();
			newTop.setAgeLock(true);
			newTop.setRemoveWhenFarAway(false);
			
			UtilEnt.vegetate(newTop, true);
			UtilEnt.ghost(newTop, true, false);
			sleigh.Host.Manager.GetCondition().Factory().Invisible("Sleigh", newTop, null, Double.MAX_VALUE, 3, false, false, true);

			top.setPassenger(newTop);
			top = newTop;
		}
	
		//Block
		if (Id != 0)
		{
			Block = Location.getWorld().spawnFallingBlock(Ent.getLocation().add(0, 1, 0), Id, (byte) Data);
			top.setPassenger(Block);
		}
	}
}
