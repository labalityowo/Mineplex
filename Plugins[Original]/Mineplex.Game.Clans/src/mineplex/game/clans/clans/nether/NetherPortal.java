package mineplex.game.clans.clans.nether;

import java.util.LinkedList;
import java.util.List;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilWorld;
import mineplex.core.common.util.UtilTime.TimeUnit;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.ClanTips.TipType;
import mineplex.game.clans.spawn.Spawn;
import mineplex.game.clans.clans.ClansManager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.event.entity.EntityPortalEnterEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.potion.PotionEffectType;

import com.google.common.collect.Lists;

/**
 * Data and listener class for individual nether portals
 */
public class NetherPortal implements Listener
{
	private static final int SECONDS_UNTIL_PORTAL = 5;
	private int _id;
	private List<Block> _frame = Lists.newArrayList();
	private List<Block> _portal = Lists.newArrayList();
	private Location _loc;
	private Location[] _corners;
	private boolean _returnPortal;
	private byte _portalFacing;
	private LinkedList<Long> _closeWarnings = new LinkedList<>();
	
	public boolean Open = false;
	public long Expire = -1;
	
	public NetherPortal(int id, Location firstCorner, Location secondCorner, boolean returnPortal)
	{
		_id = id;
		int maxX = Math.max(firstCorner.getBlockX(), secondCorner.getBlockX());
		int minX = Math.min(firstCorner.getBlockX(), secondCorner.getBlockX());
		int maxY = Math.max(firstCorner.getBlockY(), secondCorner.getBlockY());
		int minY = Math.min(firstCorner.getBlockY(), secondCorner.getBlockY());
		int maxZ = Math.max(firstCorner.getBlockZ(), secondCorner.getBlockZ());
		int minZ = Math.min(firstCorner.getBlockZ(), secondCorner.getBlockZ());
		
		for (int x = minX; x <= maxX; x++)
		{
			for (int y = minY; y <= maxY; y++)
			{
				for (int z = minZ; z <= maxZ; z++)
				{
					if (minX == maxX)
					{
						if ((y != minY && y != maxY) && (z != minZ && z != maxZ))
						{
							_portal.add(firstCorner.getWorld().getBlockAt(x, y, z));
						}
						else
						{
							_frame.add(firstCorner.getWorld().getBlockAt(x, y, z));
						}
					}
					else
					{
						if ((x != minX && x != maxX) && (y != minY && y != maxY))
						{
							_portal.add(firstCorner.getWorld().getBlockAt(x, y, z));
						}
						else
						{
							_frame.add(firstCorner.getWorld().getBlockAt(x, y, z));
						}
					}
				}
			}
		}
		
		_loc = new Location(firstCorner.getWorld(), minX + ((maxX - minX) / 2), maxY, minZ + ((maxZ - minZ) / 2));
		_corners = new Location[] {firstCorner, secondCorner};
		_returnPortal = returnPortal;
		
		if (maxX == minX)
		{
			_portalFacing = (byte)2;
		}
		else
		{
			_portalFacing = (byte)0;
		}
	}
	
	private boolean isInPortal(Block block)
	{
		return _frame.contains(block) || _portal.contains(block);
	}
	
	/**
	 * Gets the id of this portal
	 * @return This portal's id
	 */
	public int getId()
	{
		return _id;
	}
	
	/**
	 * Gets the center location of this portal
	 * @return The center location of this portal
	 */
	public Location getLocation()
	{
		return _loc;
	}
	
	/**
	 * Gets the corners of this portal
	 * @return An array of the corners of this portal
	 */
	public Location[] getCorners()
	{
		return _corners;
	}
	
	/**
	 * Checks if this portal is a return portal
	 * @return Whether this portal is a return portal
	 */
	public boolean isReturnPortal()
	{
		return _returnPortal;
	}
	
	/**
	 * Opens this portal for a given duration
	 * @param duration The duration to hold the portal open for
	 */
	@SuppressWarnings("deprecation")
	public void open(long duration)
	{
		if (Open)
		{
			if (Expire != -1)
			{
				Expire = Expire + duration;
			}
		}
		else
		{
			if (!_returnPortal)
			{
				Expire = System.currentTimeMillis() + duration;
			}
			Open = true;
			Bukkit.getPluginManager().registerEvents(this, ClansManager.getInstance().getPlugin());
			for (Block block : _frame)
			{
				block.setType(Material.OBSIDIAN);
			}
			for (Block block : _portal)
			{
				block.setType(Material.PORTAL);
				block.setData(_portalFacing);
			}
			_closeWarnings.add(UtilTime.convert(5, TimeUnit.MINUTES, TimeUnit.MILLISECONDS));
			_closeWarnings.add(UtilTime.convert(1, TimeUnit.MINUTES, TimeUnit.MILLISECONDS));
			_closeWarnings.add(30000L);
			_closeWarnings.add(10000L);
			_closeWarnings.add(5000L);
			_closeWarnings.add(4000L);
			_closeWarnings.add(3000L);
			_closeWarnings.add(2000L);
			_closeWarnings.add(1000L);
		}
	}
	
	/**
	 * Closes this portal and clears away its blocks
	 */
	public void close()
	{
		Open = false;
		Expire = -1;
		for (Block block : _portal)
		{
			block.setType(Material.AIR);
		}
		for (Block block : _frame)
		{
			block.setType(Material.AIR);
		}
		HandlerList.unregisterAll(this);
		_closeWarnings.clear();
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onBreak(BlockDamageEvent event)
	{
		if (isInPortal(event.getBlock()))
		{
			event.setInstaBreak(false);
			event.setCancelled(true);
			UtilPlayer.message(event.getPlayer(), F.main("Clans", "You cannot destroy a " + F.clansNether("Nether Portal")));
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void playerPortalEvent(PlayerPortalEvent event)
	{
		if (isInPortal(event.getFrom().getBlock()))
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler(priority = EventPriority.LOW)
	public void entityPortalEvent(EntityPortalEvent event)
	{
		if (isInPortal(event.getFrom().getBlock()))
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onEnterPortal(EntityPortalEnterEvent event)
	{
		if (event.getEntity() instanceof Player)
		{
			if (isInPortal(event.getLocation().getBlock()))
			{
				Bukkit.getScheduler().runTaskLater(ClansManager.getInstance().getPlugin(), () -> 
				{
					if (isInPortal(event.getEntity().getLocation().getBlock()))
					{
						if (isReturnPortal())
						{
							ClansManager.getInstance().getNetherManager().InNether.remove((Player)event.getEntity());
							event.getEntity().teleport(ClansManager.getInstance().getNetherManager().getReturnLocation((Player)event.getEntity()));
							ClansManager.getInstance().getNetherManager().OverworldOrigins.remove((Player)event.getEntity());
							((Player)event.getEntity()).removePotionEffect(PotionEffectType.NIGHT_VISION);
							UtilPlayer.message(event.getEntity(), F.main(ClansManager.getInstance().getNetherManager().getName(), "You have escaped " + F.clansNether("The Nether") + "!"));
							ClansManager.getInstance().getCombatManager().getLog((Player)event.getEntity()).SetLastCombatEngaged(System.currentTimeMillis() - Spawn.COMBAT_TAG_DURATION);
						}
						else
						{
							ClansManager.getInstance().getNetherManager().InNether.put((Player)event.getEntity(), Expire);
							event.getEntity().teleport(ClansManager.getInstance().getNetherManager().getNetherWorld().getSpawnLocation());
							ClansManager.getInstance().getCombatManager().getLog((Player)event.getEntity()).SetLastCombatEngaged(System.currentTimeMillis() - Spawn.COMBAT_TAG_DURATION);
							ClansManager.getInstance().ClanTips.displayTip(TipType.ENTER_NETHER, (Player)event.getEntity());
						}
					}
				}, SECONDS_UNTIL_PORTAL * 20);
			}
		}
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void onBlockPhysics(BlockPhysicsEvent event)
	{
		if (isInPortal(event.getBlock()))
		{
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void handleExpiration(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}
		if (Open && Expire != -1)
		{
			Long warning = -1L;
			for (Long test : _closeWarnings)
			{
				if ((Expire - System.currentTimeMillis()) < test)
				{
					warning = test;
					break;
				}
			}
			if (warning != -1)
			{
				_closeWarnings.remove(warning);
				Bukkit.broadcastMessage(F.main(ClansManager.getInstance().getNetherManager().getName(), "The " + F.clansNether("Nether Portal") + " at " + F.elem(UtilWorld.locToStrClean(getLocation())) + " will close in " + F.elem(UtilTime.MakeStr(warning)) + "!"));
			}
		}
		if (Open && Expire != -1 && System.currentTimeMillis() >= Expire)
		{
			close();
		}
	}
	
	@EventHandler
	public void onUnload(ChunkUnloadEvent event)
	{
		if (event.getChunk().getX() == _loc.getChunk().getX() && event.getChunk().getZ() == _loc.getChunk().getZ())
		{
			event.setCancelled(true);
		}
	}
}