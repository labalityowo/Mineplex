package mineplex.core.gadget.gadgets.item;

import java.util.HashSet;

import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Material;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.EnderPearl;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.LineFormat;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilInv;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilText;
import mineplex.core.gadget.GadgetManager;
import mineplex.core.gadget.gadgets.Ammo;
import mineplex.core.gadget.types.ItemGadget;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class ItemEtherealPearl extends ItemGadget
{
	private HashSet<String> _riding = new HashSet<String>();
	
	public ItemEtherealPearl(GadgetManager manager)
	{
		super(manager, "Ethereal Pearl", 
				UtilText.splitLineToArray(C.cWhite + "These Pearls are stolen from sleeping Endermen!", LineFormat.LORE),
				-1,  
				Material.ENDER_PEARL, (byte)0, 
				500, new Ammo("Ethereal Pearl", "50 Pearls", Material.ENDER_PEARL, (byte)0, new String[] { C.cWhite + "50 Pearls to get around with!" }, 500, 50));
	}

	@Override
	public void ActivateCustom(Player player)
	{
		player.eject();
		player.leaveVehicle();
		
		EnderPearl pearl = player.launchProjectile(EnderPearl.class);
		pearl.setPassenger(player);
	
		//Inform
		UtilPlayer.message(player, F.main("Skill", "You threw " + F.skill(getName()) + "."));
		
		//Dont Collide
		((CraftPlayer)player).getHandle().spectating = true;
		
		UtilInv.Update(player);
		
		_riding.add(player.getName());
	}
	
	@EventHandler
	public void teleportCancel(PlayerTeleportEvent event)
	{
		if (!isActive(event.getPlayer()))
			return;
		
		if (event.getCause() == TeleportCause.ENDER_PEARL)
		{
			//Firework
			FireworkEffect effect = FireworkEffect.builder().flicker(false).withColor(Color.PURPLE).with(Type.BALL).trail(true).build();

			try 
			{
				UtilFirework.playFirework(event.getTo(), effect);
			} 
			catch (Exception e) 
			{
				e.printStackTrace();
			}
			
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void disableNoCollide(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;
		
		for (Player player : UtilServer.getPlayers())
			if (_riding.contains(player.getName()))
				if (player.getVehicle() == null)
				{
					((CraftPlayer)player).getHandle().spectating = false;
					_riding.remove(player.getName());
				}
	}
	
	@EventHandler
	public void clean(PlayerQuitEvent event)
	{
		_riding.remove(event.getPlayer().getName());
	}
}
