package mineplex.hub.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionStage;
import java.util.function.Function;

import mineplex.core.MiniPlugin;
import mineplex.core.common.util.BukkitFuture;
import mineplex.core.cosmetic.CosmeticManager;
import mineplex.core.hologram.HologramManager;
import mineplex.core.mavericks.DisplaySlot;
import mineplex.core.mavericks.MavericksApprovedRepository;
import mineplex.core.mavericks.MavericksApprovedWrapper;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.hub.HubManager;
import mineplex.hub.modules.mavericks.MavericksPortalManager;
import mineplex.hub.modules.mavericks.MavericksWorldManager;
import mineplex.hub.modules.mavericks.basketball.BasketballManager;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBurnEvent;
import org.bukkit.event.block.BlockFadeEvent;
import org.bukkit.event.block.BlockFormEvent;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockGrowEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.block.BlockSpreadEvent;
import org.bukkit.event.block.EntityBlockFormEvent;
import org.bukkit.event.block.LeavesDecayEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.vehicle.VehicleEntityCollisionEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.spigotmc.event.entity.EntityMountEvent;
/**
 * A manager to handle all mavericks related content in the hub. Holds the mavericks portal manager in addition to handle
 * the rotation of the "best of" builds from Mavericks Master Builders.
 */
public class MavericksManager extends MiniPlugin
{
	
//	private static final long ROTATION_TIME = 1000*60*60*6;
	private static final long ROTATION_TIME = 1000*30;
	
	private MavericksPortalManager _portalManager;
	private MavericksWorldManager _worldManager;
	private MavericksApprovedRepository _repoApproved;
	
	private List<DisplaySlot> _displaySlots = new ArrayList<>();

	public MavericksManager(JavaPlugin plugin, CosmeticManager cosmeticManager, HologramManager hologramManager, HubManager hub)
	{
		super("Mavericks", plugin);
		
		_worldManager = new MavericksWorldManager(plugin);
		_portalManager = new MavericksPortalManager(plugin, hub, _worldManager, cosmeticManager);
		_repoApproved = new MavericksApprovedRepository();
		
		new BasketballManager(plugin, _worldManager, hub);
		
		_displaySlots.add(new DisplaySlot(new Location(_worldManager.getWorld(), -41, 24, 237), hologramManager));
		_displaySlots.add(new DisplaySlot(new Location(_worldManager.getWorld(), 19, 24, 237), hologramManager));
	}
	
	/**
	 * Main task to handle the rotation. Pulls data from the SQL DB and displays it in-game.
	 */
	@EventHandler
	public void onUpdate(UpdateEvent event)
	{
		if(event.getType() != UpdateType.MIN_01) return;
		
		Function<? super List<MavericksApprovedWrapper>, ? extends CompletionStage<Void>> updateTask = BukkitFuture.accept((list) ->
		{
			List<DisplaySlot> openSlots = new ArrayList<>();
			List<MavericksApprovedWrapper> undisplayedData = new ArrayList<>();
			undisplayedData.addAll(list);
			slots:
			for(DisplaySlot slot : _displaySlots)
			{
				if(slot.getData() != null)
				{
					for(MavericksApprovedWrapper build : list)
					{
						if(slot.getData().getBuild().getBuildId() == build.getBuild().getBuildId())
						{
							undisplayedData.remove(build);
							continue slots;
						}
					}
				}
				openSlots.add(slot);
			}
			
			for(int i = 0; i < Math.min(openSlots.size(), undisplayedData.size()); i++)
			{
				MavericksApprovedWrapper approved = undisplayedData.get(i);
				if(approved.getFirstDisplayed() == null)
				{
					_repoApproved.setDisplayDate(approved.getBuild().getBuildId());
				}
				openSlots.get(i).setData(approved);
			}
		});
		
		List<DisplaySlot> outdated = new ArrayList<>();
		for(DisplaySlot slot : _displaySlots)
		{
			if(slot.getData() == null) continue;
			
			if(slot.getData().getFirstDisplayed() == null)
			{
				_repoApproved.setDisplayDate(slot.getData().getBuild().getBuildId());
				slot.getData().setFirstDisplayed(System.currentTimeMillis());
			}
			else if(slot.getData().getFirstDisplayed() + ROTATION_TIME < System.currentTimeMillis())
			{
				outdated.add(slot);
			}
		}
		
		if(outdated.size() > 0)
		{
			_repoApproved.getToDisplay(true, outdated.size(), _displaySlots.size()).thenCompose(BukkitFuture.accept((list2) ->
			{
				if(list2.isEmpty()) return;
				
				long[] ids = new long[list2.size()];
				for(int i = 0; i < list2.size(); i++)
				{
					ids[i] = outdated.get(i).getData().getBuild().getBuildId();
				}
				_repoApproved.setDisplay(false, ids).thenCompose(BukkitFuture.accept((success) ->
				{
					_repoApproved.getToDisplay(true, _displaySlots.size(), 0).thenCompose(updateTask);
				}));
			}));
		}
		else
		{
			_repoApproved.getToDisplay(true, _displaySlots.size(), 0).thenCompose(updateTask);
		}
	}
	
	@EventHandler
	public void updateParticles(UpdateEvent event)
	{
		if(event.getType() == UpdateType.FAST)
		{
			for(DisplaySlot d : _displaySlots)
			{
				d.updateParticles();
			}
		}
	}
	
	
	@EventHandler
	public void onUnload(ChunkUnloadEvent event)
	{
		for(Entity e : event.getChunk().getEntities())
		{
			if(isStatueEntity(e))
			{
				event.setCancelled(true);
				return;
			}
		}
	}
	
	@EventHandler
	public void onGrow(BlockGrowEvent event)
	{
		if(isInsideDisplaySlot(event.getBlock()))
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onStructureGrow(StructureGrowEvent event)
	{
		for(BlockState b : event.getBlocks())
		{
			if(isInsideDisplaySlot(b.getBlock()))
			{
				event.setCancelled(true);
				return;
			}
		}
	}
	
	@EventHandler
	public void onForm(BlockFormEvent event)
	{
		if(isInsideDisplaySlot(event.getBlock()))
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onBlockFromTo(BlockFromToEvent event)
	{
		if(isInsideDisplaySlot(event.getBlock()) || isInsideDisplaySlot(event.getToBlock()))
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onSpread(BlockSpreadEvent event)
	{
		if(isInsideDisplaySlot(event.getBlock()) || isInsideDisplaySlot(event.getSource()))
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onEntityBlockFormEvent(EntityBlockFormEvent event)
	{
		if(isInsideDisplaySlot(event.getBlock()) || isStatueEntity(event.getEntity()))
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onBurn(BlockBurnEvent event)
	{
		if(isInsideDisplaySlot(event.getBlock()))
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onFade(BlockFadeEvent event)
	{
		if(isInsideDisplaySlot(event.getBlock()))
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onRedstone(BlockRedstoneEvent event)
	{
		if(isInsideDisplaySlot(event.getBlock()))
			event.setNewCurrent(event.getOldCurrent());
	}
	
	@EventHandler
	public void onDecay(LeavesDecayEvent event)
	{
		if(isInsideDisplaySlot(event.getBlock()))
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onPhysics(BlockPhysicsEvent event)
	{
		if(isInsideDisplaySlot(event.getBlock()))
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onBlockIgnite(BlockIgniteEvent event)
	{
		if(isInsideDisplaySlot(event.getBlock()) || (event.getIgnitingBlock() != null && isInsideDisplaySlot(event.getIgnitingBlock())))
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onInteract(PlayerInteractEvent event)
	{
		if(event.getClickedBlock() == null) return;
		
		if(isInsideDisplaySlot(event.getClickedBlock()))
			event.setCancelled(true);
	}
	
	
	@EventHandler
	public void ignite(EntityCombustEvent event)
	{
		if(isStatueEntity(event.getEntity()))
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onInteractEntity(PlayerInteractEntityEvent event)
	{
		if(isStatueEntity(event.getRightClicked()))
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onInteractEntity(PlayerInteractAtEntityEvent event)
	{
		if(isStatueEntity(event.getRightClicked()))
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onVehicleCollide(VehicleEntityCollisionEvent event)
	{
		if(isStatueEntity(event.getVehicle()))
			event.setCancelled(true);
	}
	
	@EventHandler
	public void onMount(EntityMountEvent event)
	{
		if(isStatueEntity(event.getMount())) 
			event.setCancelled(true);
	}
	
	public boolean isInsideDisplaySlot(Block block)
	{
		return isInsideDisplaySlot(block.getLocation().add(0.5, 0.5, 0.5));
	}
	
	public boolean isInsideDisplaySlot(Location loc)
	{
		for(DisplaySlot d : _displaySlots)
		{
			if(d.isInside(loc)) return true;
		}
		return false;
	}
	
	/**
	 * Check if an entity is spawned in and managed by the mavericks "best of" builds.
	 * @param e The entity you want to check.
	 * @return Returns true if the entity is spawned and managed by this mavericks manager.
	 */
	public boolean isStatueEntity(Entity e)
	{
		for(DisplaySlot d : _displaySlots)
		{
			if(d.isDisplaySlotEntity(e)) return true;
		}
		return false;
	}
	
	public MavericksPortalManager getPortalManager()
	{
		return _portalManager;
	}

}
