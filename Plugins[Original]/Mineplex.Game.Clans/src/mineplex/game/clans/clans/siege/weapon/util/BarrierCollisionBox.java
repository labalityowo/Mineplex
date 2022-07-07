package mineplex.game.clans.clans.siege.weapon.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.BiConsumer;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;

import mineplex.core.common.util.UtilBlock;
import mineplex.core.common.util.UtilCollections;
import mineplex.core.common.util.UtilServer;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

public class BarrierCollisionBox implements Listener
{
	private List<Location> _collisionBlocks;
	
	private List<BiConsumer<Location, Player>> _leftClickListeners;
	private List<BiConsumer<Location, Player>> _rightClickListeners;
	
	BarrierCollisionBox()
	{
		_collisionBlocks = new ArrayList<>();
		
		_leftClickListeners = new ArrayList<>();
		_rightClickListeners = new ArrayList<>();
	}
	
	BarrierCollisionBox(List<Location> locations)
	{
		this();
		
		_collisionBlocks.addAll(locations);
	}
	
	BarrierCollisionBox(Location start, Location end)
	{
		this();
		
		UtilBlock.getInBoundingBox(start.getBlock().getLocation(), end.getBlock().getLocation(), false).forEach(block -> _collisionBlocks.add(block.getLocation()));
	}
	
	public void Construct()
	{
		setBlocks();
		
		UtilServer.RegisterEvents(this);
	}
	
	public void Destruct()
	{
		HandlerList.unregisterAll(this);
		
		resetBlocks();
		
		_leftClickListeners.clear();
		_rightClickListeners.clear();
		_collisionBlocks.clear();
	}
	
	public void Update()
	{
		setBlocks();
	}
	
	public void registerLeft(BiConsumer<Location, Player> listener)
	{
		_leftClickListeners.add(listener);
	}
	
	public void registerRight(BiConsumer<Location, Player> listener)
	{
		_rightClickListeners.add(listener);
	}
	
	public void unregisterLeft(BiConsumer<Location, Player> listener)
	{
		_leftClickListeners.remove(listener);
	}
	
	public void unregisterRight(BiConsumer<Location, Player> listener)
	{
		_rightClickListeners.remove(listener);
	}
	
	private void onLeftClick(Location location, Player player)
	{
		UtilCollections.ForEach(_leftClickListeners, listener -> listener.accept(location, player));
	}
	
	private void onRightClick(Location location, Player player)
	{
		UtilCollections.ForEach(_rightClickListeners, listener -> listener.accept(location, player));
	}
	
	private void resetBlocks()
	{
		_collisionBlocks
			.stream()
				.filter(location -> location.getBlock().getType().equals(Material.BARRIER))
			.forEach(location -> location.getBlock().setType(Material.AIR));
	}
	
	private void setBlocks()
	{
		for (Location location : _collisionBlocks)
		{
			location.getBlock().setType(Material.BARRIER);
		}
	}
	
	public boolean isInBox(Location location)
	{
		for (Location other : _collisionBlocks)
		{
			if (other.equals(location))
			{
				return true;
			}
		}
		
		return false;
	}
	
	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() == UpdateType.FAST)
		{
			Update();
		}
	}
	
	// Events for interaction with the collision box;
	@EventHandler
	public void blockDamage(BlockDamageEvent event)
	{
		if (isInBox(event.getBlock().getLocation()))
		{
			onLeftClick(event.getBlock().getLocation(), event.getPlayer());
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void blockBreak(BlockBreakEvent event)
	{
		if (isInBox(event.getBlock().getLocation()))
		{
			onLeftClick(event.getBlock().getLocation(), event.getPlayer());
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void interact(PlayerInteractEvent event)
	{
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
		{
			return;
		}
		
		if (isInBox(event.getClickedBlock().getLocation()))
		{
			onRightClick(event.getClickedBlock().getLocation(), event.getPlayer());
			event.setCancelled(true);
		}
	}
	
	public BarrierCollisionBox add(Location... location)
	{
		_collisionBlocks.addAll(Arrays.asList(location));
		
		Update();
		
		return this;
	}
	
	public BarrierCollisionBox add(List<Location> location)
	{
		_collisionBlocks.addAll(location);
		
		Update();
		
		return this;
	}
	
	public BarrierCollisionBox remove(Location... location)
	{
		_collisionBlocks.removeAll(Arrays.asList(location));
		
		Update();
		
		return this;
	}
	
	public BarrierCollisionBox add(BarrierCollisionBox box, boolean destructOld, boolean cloneListeners)
	{
		_collisionBlocks.addAll(box._collisionBlocks);
		
		if (cloneListeners)
		{
			_leftClickListeners.addAll(box._leftClickListeners);
			_rightClickListeners.addAll(box._rightClickListeners);
		}
		
		if (destructOld)
		{
			box.Destruct();
		}
		
		Update();
		
		return this;
	}
	
	public BarrierCollisionBox add(BarrierCollisionBox box)
	{
		return add(box, false, false);
	}
	
	public BarrierCollisionBox addAll(Location start, Location end)
	{
		UtilBlock.getInBoundingBox(start, end).forEach(block -> _collisionBlocks.add(block.getLocation()));
		
		Update();
		
		return this;
	}
	
	public static BarrierCollisionBox all(Location start, Location end)
	{
		return new BarrierCollisionBox(start, end);
	}
	
	public static BarrierCollisionBox single(Location location)
	{
		return new BarrierCollisionBox(new ArrayList<>(Arrays.asList(location.getBlock().getLocation())));
	}
}