package mineplex.core.blood;

import java.util.HashMap;
import java.util.HashSet;

import mineplex.core.MiniPlugin;
import mineplex.core.common.util.UtilServer;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.core.updater.UpdateType;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryPickupItemEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

public class Blood extends MiniPlugin
{
	private HashMap<Item, Integer> _blood = new HashMap<Item, Integer>();

	public Blood(JavaPlugin plugin) 
	{
		super("Blood", plugin);
	}

	@EventHandler
	public void Death(PlayerDeathEvent event)
	{
		Effects(event.getEntity(), event.getEntity().getEyeLocation(), 10, 0.5, Sound.HURT_FLESH, 1f, 1f, Material.INK_SACK, (byte)1, true);
	}
	
	public void Effects(Player player, Location loc, int particles, double velMult, Sound sound,
			float soundVol, float soundPitch, Material type, byte data, boolean bloodStep)
	{
		Effects(player, loc, particles, velMult, sound, soundVol, soundPitch, type, data, 10, bloodStep);
	}
	
	public void Effects(Player player, Location loc, int particles, double velMult, Sound sound,
			float soundVol, float soundPitch, Material type, byte data, int ticks, boolean bloodStep)
	{
		BloodEvent event = new BloodEvent(player, loc, particles, velMult, sound, soundVol, soundPitch, type, data, ticks, bloodStep);
		UtilServer.getServer().getPluginManager().callEvent(event);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void display(BloodEvent event)
	{
		if(event.isCancelled()) return;
		
		if(event.getMaterial() != null && event.getMaterial() != Material.AIR)
		{
			for (int i = 0 ; i < event.getParticles() ; i++)
			{
				Item item = event.getLocation().getWorld().dropItem(event.getLocation(), 
						new ItemBuilder(event.getMaterial(), 1, event.getMaterialData()).setTitle("" + System.nanoTime()).build());
				
				item.setVelocity(new Vector((Math.random() - 0.5)*event.getVelocityMult(),Math.random()*event.getVelocityMult(),(Math.random() - 0.5)*event.getVelocityMult()));
	
				item.setPickupDelay(999999);
	
				_blood.put(item, event.getTicks());
			}
		}

		if (event.getBloodStep())
			event.getLocation().getWorld().playEffect(event.getLocation(), Effect.STEP_SOUND, 55);
		
		if(event.getSound() != null)
			event.getLocation().getWorld().playSound(event.getLocation(), event.getSound(), event.getSoundVolume(), event.getSoundPitch());
	}

	@EventHandler
	public void Update(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		HashSet<Item> expire = new HashSet<Item>();

		for (Item cur : _blood.keySet())
			if (cur.getTicksLived() > _blood.get(cur) || !cur.isValid())
				expire.add(cur);

		for (Item cur : expire)
		{
			cur.remove();
			_blood.remove(cur);
		}
	}

	@EventHandler
	public void Pickup(PlayerPickupItemEvent event)
	{
		if (event.isCancelled())
			return;
		
		if (_blood.containsKey(event.getItem()))
			event.setCancelled(true);
	}
	
	@EventHandler
	public void HopperPickup(InventoryPickupItemEvent event)
	{
		if (event.isCancelled())
			return;
		
		if (_blood.containsKey(event.getItem()))
			event.setCancelled(true);
	}
}
