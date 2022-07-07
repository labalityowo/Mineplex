package mineplex.gemhunters.util;

import java.util.function.Consumer;

import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.plugin.java.JavaPlugin;

import mineplex.core.common.util.UtilEnt;

public class SimpleNPC implements Listener
{

	protected final LivingEntity _entity;
	private final Consumer<Player> _clickEvent;
	private final boolean _vegetated;

	public SimpleNPC(JavaPlugin plugin, Location spawn, Class<? extends LivingEntity> type, String name, Consumer<Player> clickEvent)
	{
		this(plugin, spawn, type, name, clickEvent, true);
	}

	public SimpleNPC(JavaPlugin plugin, Location spawn, Class<? extends LivingEntity> type, String name, Consumer<Player> clickEvent, boolean vegetated)
	{
		spawn.getWorld().loadChunk(spawn.getChunk());
		_entity = spawn.getWorld().spawn(spawn, type);

		_entity.setRemoveWhenFarAway(false);
		_entity.setCustomName(name);
		_entity.setCustomNameVisible(true);

		UtilEnt.vegetate(_entity, true);
		UtilEnt.ghost(_entity, true, false);
		UtilEnt.setFakeHead(_entity, true);

		_clickEvent = clickEvent;
		_vegetated = vegetated;

		plugin.getServer().getPluginManager().registerEvents(this, plugin);
	}

	@EventHandler
	public void npcClick(PlayerInteractEntityEvent event)
	{
		if (!event.getRightClicked().equals(_entity))
		{
			return;
		}

		event.setCancelled(true);

		if (_clickEvent != null)
		{
			_clickEvent.accept(event.getPlayer());
		}
	}

	@EventHandler
	public void npcDamage(EntityDamageEvent event)
	{
		if (!event.getEntity().equals(_entity) || !_vegetated)
		{
			return;
		}

		event.setCancelled(true);
	}

	@EventHandler
	public void npcDeath(EntityDeathEvent event)
	{
		if (!event.getEntity().equals(_entity) || !_vegetated)
		{
			return;
		}

		HandlerList.unregisterAll(this);
	}
	
	public final LivingEntity getEntity() 
	{
		return _entity;
	}

}
