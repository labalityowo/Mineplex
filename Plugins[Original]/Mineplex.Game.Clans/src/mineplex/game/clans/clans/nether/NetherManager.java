package mineplex.game.clans.clans.nether;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.google.common.collect.Lists;

import mineplex.core.MiniPlugin;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.generator.VoidGenerator;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilTime.TimeUnit;
import mineplex.core.common.util.UtilWorld;
import mineplex.core.itemstack.ItemBuilder;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.event.ClansCommandExecutedEvent;
import mineplex.game.clans.clans.nether.command.ForceTeleportCommand;
import mineplex.game.clans.clans.nether.command.PortalCommand;
import mineplex.game.clans.clans.nether.data.ClaimData;
import mineplex.game.clans.clans.nether.miniboss.NetherMinibossManager;
import mineplex.game.clans.clans.worldevent.boss.BossDeathEvent;
import mineplex.game.clans.spawn.Spawn;

/**
 * Manager for all nether features
 */
public class NetherManager extends MiniPlugin
{
	public enum Perm implements Permission
	{
		PORTAL_COMMAND,
		PORTAL_CLOSE_COMMAND,
		PORTAL_CREATE_COMMAND,
		PORTAL_DELETE_COMMAND,
		PORTAL_FORCE_COMMAND,
		PORTAL_LIST_COMMAND,
		PORTAL_OPEN_COMMAND,
	}
	
	private static final long PORTAL_OPEN_DURATION = UtilTime.convert(10, TimeUnit.MINUTES, TimeUnit.MILLISECONDS);
	private static final String CLAIM_WAND_NAME = C.cRedB + "Portal Claim Wand";
	private static final String[] CLAIM_WAND_LORE = new String[] {C.cYellow + "Left Click to select the Portal's first corner", C.cYellow + "Right Click to select the Portal's second corner"};
	private static final ItemStack CLAIM_WAND = new ItemBuilder(Material.WOOD_AXE).setTitle(CLAIM_WAND_NAME).setLore(CLAIM_WAND_LORE).build();
	private static final int SPAWN_MIN_X = 30;
	private static final int SPAWN_MIN_Z = 99;
	private static final int SPAWN_MAX_X = 56;
	private static final int SPAWN_MAX_Z = 115;
	
	private PortalRepository _repo;
	private NetherMinibossManager _miniboss;
	private World _netherWorld;
	private List<NetherPortal> _portals = new ArrayList<>();
	public List<BossNetherPortal> BossPortals = new ArrayList<>();
	private List<NetherPortal> _returnPortals = new ArrayList<>();
	public Map<Player, Long> InNether = new HashMap<>();
	public Map<Player, Location> OverworldOrigins = new HashMap<>();
	public Map<Player, ClaimData> Claiming = new HashMap<>();
	
	public NetherManager(ClansManager manager)
	{
		super("Nether", manager.getPlugin());
		
		begin();
		_miniboss = new NetherMinibossManager(this);
		addCommand(new PortalCommand(this));
		addCommand(new ForceTeleportCommand(this));
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{

		PermissionGroup.ADMIN.setPermission(Perm.PORTAL_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.PORTAL_CLOSE_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.PORTAL_CREATE_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.PORTAL_DELETE_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.PORTAL_FORCE_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.PORTAL_LIST_COMMAND, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.PORTAL_OPEN_COMMAND, true, true);
	}
	
	private void begin()
	{
		if (Bukkit.getWorld("nether") == null)
		{
			WorldCreator creator = new WorldCreator("nether");
			creator.generator(new VoidGenerator());
			Bukkit.createWorld(creator);
		}
		_netherWorld = Bukkit.getWorld("nether");
		_netherWorld.setSpawnLocation(43, 135, 113);
		WorldBorder worldBorder = _netherWorld.getWorldBorder();
		worldBorder.setCenter(0, 0);
		worldBorder.setSize(800 * 2);
		
		_repo = new PortalRepository(getPlugin(), this);
		loadPortals();
	}
	
	private void loadPortals()
	{
		_repo.loadPortals();
	}
	
	@Override
	public void disable()
	{
		closePortals();
		for (Player player : InNether.keySet())
		{
			player.teleport(Spawn.getNorthSpawn());
		}
		InNether.clear();
	}
	
	/**
	 * Get the manager for nether miniboss
	 * @return The loaded Nether Miniboss manager
	 */
	public NetherMinibossManager getMinibossManager()
	{
		return _miniboss;
	}
	
	/**
	 * Gets the Nether world
	 * @return The Nether world
	 */
	public World getNetherWorld()
	{
		return _netherWorld;
	}
	
	/**
	 * Checks if a player is in the nether
	 * @param player The player to check
	 * @return Whether the player is in the nether
	 */
	public boolean isInNether(Player player)
	{
		return isInNether(player.getLocation());
	}
	
	/**
	 * Checks if a location is in the nether
	 * @param location The location to check
	 * @return Whether the player is in the nether
	 */
	public boolean isInNether(Location location)
	{
		return location.getWorld().equals(_netherWorld);
	}
	
	/**
	 * Checks if a location is inside Nether Spawn
	 * @param loc The location to check
	 * @return true if the location is inside the Nether Spawn
	 */
	public boolean isInSpawn(Location loc)
	{
		if (loc.getWorld().equals(getNetherWorld()))
		{
			if (loc.getBlockX() >= SPAWN_MIN_X && loc.getBlockX() <= SPAWN_MAX_X)
			{
				if (loc.getBlockZ() >= SPAWN_MIN_Z && loc.getBlockZ() <= SPAWN_MAX_Z)
				{
					return true;
				}
			}
		}
		return false;
	}
	
	/**
	 * Gets the place a player will exit the nether
	 * @param player The player to check
	 * @return The place the player will exit the nether
	 */
	public Location getReturnLocation(Player player)
	{
		Location defaultLoc = Spawn.getWestTown();
		if (UtilMath.random.nextDouble() <= .5)
		{
			defaultLoc = Spawn.getEastTown();
		}
		return OverworldOrigins.getOrDefault(player, defaultLoc);
	}
	
	/**
	 * Fetches the nether portal with the given id
	 * @param id The id of the portal
	 * @return The nether portal with the given id
	 */
	public NetherPortal getPortal(int id)
	{
		for (NetherPortal portal : _portals)
		{
			if (portal.getId() == id)
			{
				return portal;
			}
		}
		
		return null;
	}
	
	public List<NetherPortal> getReturnPortals()
	{
		return _returnPortals;
	}
	
	/**
	 * Loads a nether portal into the manager
	 * @param portal The portal to load
	 */
	public void addPortal(NetherPortal portal)
	{
		_portals.add(portal);
		if (portal.isReturnPortal())
		{
			_returnPortals.add(portal);
		}
		_portals.sort(new Comparator<NetherPortal>()
		{
			public int compare(NetherPortal o1, NetherPortal o2)
			{
				if (o1.getId() > o2.getId())
				{
					return 1;
				}
				return -1;
			}
		});
	}
	
	/**
	 * Deletes a nether portal and removes it from the database
	 * @param portal The portal to remove
	 */
	public void deletePortal(NetherPortal portal)
	{
		portal.close();
		_portals.remove(portal);
		_returnPortals.remove(portal);
		_repo.deletePortal(portal.getId());
	}
	
	/**
	 * Spawns a nether portal for a given duration
	 * @param duration The duration to maintain the portal for
	 */
	public void spawnPortal(long duration)
	{
		if (_portals.isEmpty() || _returnPortals.isEmpty())
		{
			return;
		}
		List<NetherPortal> available = Lists.newArrayList();
		available.addAll(_portals);
		for (NetherPortal remove : _returnPortals)
		{
			available.remove(remove);
		}
		if (available.isEmpty())
		{
			return;
		}
		NetherPortal portal = available.get(UtilMath.r(available.size()));
		portal.open(duration);
		for (NetherPortal returnPortal : _returnPortals)
		{
			returnPortal.open(-1);
		}
		UtilTextMiddle.display(F.clansNether("Nether Portal"), "Has opened at " + F.elem(UtilWorld.locToStrClean(portal.getLocation())));
		Bukkit.broadcastMessage(F.main(getName(), "A " + F.clansNether("Nether Portal") + " has opened at " + F.elem(UtilWorld.locToStrClean(portal.getLocation())) + " for " + F.elem(UtilTime.MakeStr(duration)) + "!"));
	}
	
	/**
	 * Spawns a portal for the default remain duration
	 */
	public void spawnPortal()
	{
		spawnPortal(PORTAL_OPEN_DURATION);
	}
	
	/**
	 * Spawns a nether portal when a boss dies
	 * @param bossSpawn The location where the boss spawned in
	 */
	public void spawnBossPortal(Location bossSpawn)
	{
		if (_returnPortals.isEmpty())
		{
			return;
		}
		BossNetherPortal portal = new BossNetherPortal(bossSpawn.clone().add(-2, 5, 0), bossSpawn.clone().add(2, 0, 0), false);
		portal.open(PORTAL_OPEN_DURATION);
		BossPortals.add(portal);
		for (NetherPortal returnPortal : _returnPortals)
		{
			returnPortal.open(-1);
		}
		UtilTextMiddle.display(F.clansNether("Nether Portal"), "Has opened at " + F.elem(UtilWorld.locToStrClean(portal.getLocation())));
		Bukkit.broadcastMessage(F.main(getName(), "A " + F.clansNether("Nether Portal") + " has opened at " + F.elem(UtilWorld.locToStrClean(portal.getLocation())) + " for " + F.elem(UtilTime.MakeStr(PORTAL_OPEN_DURATION)) + "!"));
	}
	
	/**
	 * Creates a portal with the player's stored corners
	 * @param creator The creator of the portal
	 * @param returnPortal Whether the portal is a return portal
	 */
	public void createPortal(Player creator, boolean returnPortal)
	{
		if (Claiming.getOrDefault(creator, new ClaimData()).getTotalSelected() < 2)
		{
			UtilPlayer.message(creator, F.main(getName(), "You do not have a top and bottom corner selected!"));
			return;
		}
		
		ClaimData data = Claiming.remove(creator);
		_repo.addPortal(UtilWorld.locToStr(data.getFirstCorner().getLocation()), UtilWorld.locToStr(data.getSecondCorner().getLocation()), returnPortal);
		UtilPlayer.message(creator, F.main(getName(), "Portal successfully created!"));
	}
	
	/**
	 * Closes all portals and clears away their blocks
	 */
	public void closePortals()
	{
		for (NetherPortal portal : _portals)
		{
			portal.close();
		}
		for (BossNetherPortal portal : BossPortals)
		{
			portal.close();
		}
		BossPortals.clear();
	}
	
	/**
	 * Displays a list of all portals to a player
	 * @param player The player to display the list to
	 */
	public void showPortalList(Player player)
	{
		UtilPlayer.message(player, F.main(getName(), "Portal List:"));
		for (NetherPortal portal : _portals)
		{	
			UtilPlayer.message(player, C.cBlue + "- " + F.elem("Portal " + portal.getId() + ": " + C.cGray + UtilWorld.locToStrClean(portal.getLocation()).replace("(", "").replace(")", "")));
		}
	}
	
	/**
	 * Gives a player a portal claim wand
	 * @param player The player to give the wand to
	 */
	public void giveWand(Player player)
	{
		player.getInventory().addItem(CLAIM_WAND.clone());
		UtilPlayer.message(player, F.main(getName(), "You have been given a Portal Claim Wand!"));
	}
	
	@EventHandler
	public void breakBlock(BlockBreakEvent event)
	{
		Block block = event.getBlock();
		Player player = event.getPlayer();
		
		if (player.getGameMode() == GameMode.CREATIVE)
		{
			return;
		}
		
		if (!block.getWorld().equals(_netherWorld))
		{
			return;
		}
		
		event.setCancelled(true);
		UtilPlayer.message(player, F.main(getName(), "You cannot build in " + F.clansNether("The Nether") + "!"));
	}
	
	@EventHandler
	public void placeBlock(BlockPlaceEvent event)
	{
		Block block = event.getBlock();
		Player player = event.getPlayer();
		
		if (player.getGameMode() == GameMode.CREATIVE)
		{
			return;
		}
		
		if (!block.getWorld().equals(_netherWorld))
		{
			return;
		}
		
		event.setCancelled(true);
		UtilPlayer.message(player, F.main(getName(), "You cannot build in " + F.clansNether("The Nether") + "!"));
	}
	
	@EventHandler(priority = EventPriority.HIGHEST)
	public void onPortal(PlayerPortalEvent event)
	{
		if (event.getTo() == null || event.getTo().getWorld().equals(_netherWorld))
		{
			return;
		}
		event.setCancelled(true);
		runSyncLater(() ->
		{
			InNether.remove(event.getPlayer());
			event.getPlayer().teleport(getReturnLocation(event.getPlayer()));
			OverworldOrigins.remove(event.getPlayer());
			event.getPlayer().removePotionEffect(PotionEffectType.NIGHT_VISION);
			UtilPlayer.message(event.getPlayer(), F.main(getName(), "You have escaped " + F.clansNether("The Nether") + "!"));
			ClansManager.getInstance().getCombatManager().getLog(event.getPlayer()).SetLastCombatEngaged(System.currentTimeMillis() - Spawn.COMBAT_TAG_DURATION);
		}, 1);
	}
	
	@EventHandler
	public void onPortal(EntityPortalEvent event)
	{
		event.setCancelled(true);
	}
	
	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() == UpdateType.FAST)
		{
			List<Player> netherKeys = Lists.newArrayList();
			netherKeys.addAll(InNether.keySet());
			for (Player player : netherKeys)
			{
				if (System.currentTimeMillis() >= InNether.get(player))
				{
					InNether.remove(player);
					if (isInNether(player))
					{
						player.teleport(getReturnLocation(player));
						OverworldOrigins.remove(player);
						player.removePotionEffect(PotionEffectType.NIGHT_VISION);
						ClansManager.getInstance().getCombatManager().getLog(player).SetLastCombatEngaged(System.currentTimeMillis() - Spawn.COMBAT_TAG_DURATION);
						UtilPlayer.message(player, F.main(getName(), "You have been forced to escape " + F.clansNether("The Nether") + " to survive its demonic poisons!"));
					}
				}
				else
				{
					if (!player.hasPotionEffect(PotionEffectType.NIGHT_VISION))
					{
						int ticks = (int)((InNether.get(player) - System.currentTimeMillis()) / 1000) * 20;
						player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, ticks, 0));
					}
				}
			}
			
			UtilServer.getPlayersCollection()
				.stream()
				.filter(player -> isInNether(player))
				.forEach(player -> ClansManager.getInstance().getItemMapManager().removeMap(player));
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onQuit(PlayerQuitEvent event)
	{
		if (isInNether(event.getPlayer()))
		{
			InNether.remove(event.getPlayer());
			event.getPlayer().teleport(getReturnLocation(event.getPlayer()));
			OverworldOrigins.remove(event.getPlayer());
			event.getPlayer().removePotionEffect(PotionEffectType.NIGHT_VISION);
		}
		Claiming.remove(event.getPlayer());
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onDie(PlayerDeathEvent event)
	{
		InNether.remove(event.getEntity());
		OverworldOrigins.remove(event.getEntity());
		Claiming.remove(event.getEntity());
	}
	
	@EventHandler
	public void onTpHome(ClansCommandExecutedEvent event)
	{
		if (!isInNether(event.getPlayer()))
		{
			return;
		}
		if (event.getCommand().equalsIgnoreCase("tphome") || event.getCommand().equalsIgnoreCase("stuck"))
		{
			event.setCancelled(true);
			UtilPlayer.message(event.getPlayer(), F.main(getName(), "You cannot teleport while in " + F.clansNether("The Nether") + "!"));
		}
		if (event.getCommand().equalsIgnoreCase("claim") || event.getCommand().equalsIgnoreCase("unclaim") || event.getCommand().equalsIgnoreCase("unclaimall") || event.getCommand().equalsIgnoreCase("homeset"))
		{
			event.setCancelled(true);
			UtilPlayer.message(event.getPlayer(), F.main(getName(), "You cannot manage your clan's territory while in " + F.clansNether("The Nether") + "!"));
		}
	}
	
	@EventHandler
	public void onDropWand(PlayerDropItemEvent event)
	{
		ItemStack item = event.getItemDrop().getItemStack();
		if (item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().equalsIgnoreCase(CLAIM_WAND_NAME))
		{
			runSyncLater(() ->
			{
				event.getItemDrop().remove();
			}, 1L);
		}
	}
	
	@EventHandler
	public void onUseWand(PlayerInteractEvent event)
	{
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_BLOCK)
		{
			return;
		}
		if (!event.hasBlock() || !event.hasItem())
		{
			return;
		}
		if (!ClansManager.getInstance().getClientManager().Get(event.getPlayer()).hasPermission(Perm.PORTAL_CREATE_COMMAND))
		{
			return;
		}
		ItemStack item = event.getItem();
		if (item.getItemMeta().hasDisplayName() && item.getItemMeta().getDisplayName().equalsIgnoreCase(CLAIM_WAND_NAME))
		{
			Block block = event.getClickedBlock();
			if (!Claiming.containsKey(event.getPlayer()))
			{
				Claiming.put(event.getPlayer(), new ClaimData());
			}
			ClaimData data = Claiming.get(event.getPlayer());
			if (event.getAction() == Action.RIGHT_CLICK_BLOCK)
			{
				data.setSecondCorner(block);
				UtilPlayer.message(event.getPlayer(), F.main(getName(), "You have selected the Portal's second corner!"));
			}
			else
			{
				data.setFirstCorner(block);
				UtilPlayer.message(event.getPlayer(), F.main(getName(), "You have selected the Portal's first corner!"));
			}
			event.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBossDeath(BossDeathEvent event)
	{
		spawnBossPortal(event.getEvent().getCenterLocation().clone());
	}
	
	@EventHandler
	public void onBlockDamage(EntityExplodeEvent event)
	{
		if (isInNether(event.getLocation()))
		{
			event.setYield(0f);
		}
	}
}