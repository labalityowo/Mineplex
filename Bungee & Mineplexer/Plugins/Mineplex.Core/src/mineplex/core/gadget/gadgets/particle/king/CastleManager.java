package mineplex.core.gadget.gadgets.particle.king;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.spigotmc.event.entity.EntityDismountEvent;

import mineplex.core.MiniPlugin;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAction;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilColor;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilWorld;
import mineplex.core.gadget.event.GadgetEnableEvent;
import mineplex.core.gadget.gadgets.particle.king.types.King;
import mineplex.core.gadget.gadgets.particle.king.types.Peasant;
import mineplex.core.gadget.types.GadgetType;
import mineplex.core.hologram.Hologram;
import mineplex.core.hologram.HologramManager;
import mineplex.core.pet.event.PetSpawnEvent;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

/**
 * Manages the castle in the main lobbies
 */
public class CastleManager extends MiniPlugin
{
	private King _king;

	private Location _castleLocationA = new Location(UtilWorld.getWorld("world"), 32, 71, -4);
	private Location _castleLocationB = new Location(UtilWorld.getWorld("world"), 44, 82, 8);

	private Location _removedKingLocation = new Location(UtilWorld.getWorld("world"), 28, 71, 6);

	private Location _hologramLocation = new Location(UtilWorld.getWorld("world"), 28.5, 74, 2.5);

	private Location[] _woolLocations = new Location[]
			{
				new Location(UtilWorld.getWorld("world"), 33, 80, -3),
				new Location(UtilWorld.getWorld("world"), 33, 80, 7),
				new Location(UtilWorld.getWorld("world"), 43, 80, -3),
				new Location(UtilWorld.getWorld("world"), 43, 80, 7)
			};

	private Location[] _throneLocations = new Location[]
			{
					new Location(UtilWorld.getWorld("world"), 41, 76, 2),
					new Location(UtilWorld.getWorld("world"), 42, 77, 2),
					new Location(UtilWorld.getWorld("world"), 42, 78, 2),
			};

	private CoreClientManager _coreClientManager;
	private HologramManager _hologramManager;

	private Hologram _hologram;

	private boolean _isHub = false;

	private Map<Player, King> _kings = new HashMap<>();
	private Map<Player, Peasant> _peasants = new HashMap<>();

	private Item _item;

	public CastleManager(JavaPlugin plugin, CoreClientManager coreClientManager, HologramManager hologramManager, boolean isHub)
	{
		super("CastleManager", plugin);
		_coreClientManager = coreClientManager;
		_hologramManager = hologramManager;
		_isHub = isHub;
		spawnHologram();
	}

	/**
	 * Sets the lobby's king
	 * @param king The new king
	 */
	public void setKing(King king)
	{
		_king = king;
		updateHologram();
	}

	/**
	 * Gets the current king
	 * @return The king of that lobby
	 */
	public King getKing()
	{
		return _king;
	}

	/**
	 * Checks if the server is a hub server or not
	 * @return
	 */
	public boolean isHub()
	{
		return _isHub;
	}

	/**
	 * Stops entities from spawning inside the castle
	 * @param event
	 */
	@EventHandler
	public void onEntitySpawn(EntitySpawnEvent event)
	{
		if (!isHub())
			return;

		if (event.getEntity() instanceof Player)
			return;

		if (event.getEntity() instanceof Item)
			return;

		if (isInsideCastle(event.getLocation()))
		{
			event.setCancelled(true);
		}
	}

	/**
	 * Stops players from walking inside the castle
	 * @param event
	 */
	@EventHandler
	public void onPlayerMove(UpdateEvent event)
	{
		if (!isHub())
			return;

		if (event.getType() != UpdateType.TICK)
			return;

		for (Player player : UtilServer.getPlayers())
		{
			if (_king != null)
				if (_king.getKing().equals(player))
					continue;
			if (isInsideCastle(player.getLocation()))
			{
				Entity target = player;
				while (target.getVehicle() != null)
				{
					target = target.getVehicle();
				}
				Location spawnLocation = new Location(target.getWorld(), 0, 64, 0);
				UtilAction.velocity(target, UtilAlg.getTrajectory(target.getLocation(), spawnLocation).multiply(-1), 1.5, true, 0.8, 0, 1.0, true);
			}
		}
	}

	/**
	 * Detects when a player right clicks another player to tag them as their peasant
	 * @param event
	 */
	@EventHandler
	public void onTagPeasant(PlayerInteractEntityEvent event)
	{
		if (!isHub())
			return;

		Player clicker = event.getPlayer();

		if (!isKing(clicker))
			return;

		if (!(event.getRightClicked() instanceof Player))
			return;

		Player clicked = (Player) event.getRightClicked();

		if (!isPeasant(clicked))
			return;

		King king = getKing(clicker);
		Peasant peasant = getPeasant(clicked);

		if (peasant.isInCooldown())
		{
			UtilPlayer.message(clicker, F.main("Kingdom", "You cannot claim " + F.name(clicked.getName()) + " because they have been claimed too recently! Try again later."));
			return;
		}

		if (king.hasPeasant(peasant))
		{
			UtilPlayer.message(clicker, F.main("Kingdom", "You cannot claim " + F.name(clicked.getName()) + " because they are already a member of your kingdom!"));
			return;
		}

		if (peasant.getKing() != null)
		{
			King oldKing = peasant.getKing();
			oldKing.removePeasant(peasant);
			UtilPlayer.message(oldKing.getKing(), F.main("Kingdom", "" + F.name(clicked.getName()) + ", a member of your kingdom, was kidnapped by the evil monarch " + F.name(clicker.getName()) + "!"));
		}

		peasant.setCooldown();
		king.addPeasant(peasant);
		peasant.setKing(king);
		UtilPlayer.message(clicker, F.main("Kingdom", "You claimed " + F.name(clicked.getName()) + " as a member of your kingdom!"));
		UtilPlayer.message(clicker, F.main("Kingdom", "Kingdom population: " + F.count(king.amountOfPeasants())));
		UtilPlayer.message(clicker, F.main("Kingdom", "Kingdom position: " + F.count("#" + getKingPosition(king))));
		UtilPlayer.message(peasant.getPeasant(), F.main("Kingdom", "" + F.name(clicker.getName()) + " claimed you as a member of their kingdom!"));

		updateLobbyKing();
		updateHologram();
	}

	/**
	 * Lets player sit on throne
	 * @param event
	 */
	@EventHandler
	public void onSitThrone(PlayerInteractEvent event)
	{
		Player player = event.getPlayer();

		if (!isKing(player))
			return;

		if (!getKing().getKing().equals(player))
			return;

		if (event.getAction() != Action.RIGHT_CLICK_BLOCK)
			return;

		if (!isHub())
			return;

		Block block = event.getClickedBlock();

		boolean isThrone = false;
		for (Location location : _throneLocations)
		{
			if (isThrone)
				break;

			isThrone = block.getLocation().getBlockX() == location.getBlockX()
					&& block.getLocation().getBlockY() == location.getBlockY()
					&& block.getLocation().getBlockZ() == location.getBlockZ();
		}

		if (!isThrone)
			return;

		Location loc = _throneLocations[0].clone().add(0.5, 1, 0.5);
		loc.setYaw(90);
		loc.setPitch(0);
		Item item = loc.getWorld().dropItem(loc, new ItemStack(Material.WOOL, 1, (byte) 14));
		item.setPickupDelay(Integer.MAX_VALUE);
		item.setVelocity(new Vector(0, 0, 0));
		item.teleport(loc);
		item.setPassenger(player);
		_item = item;
	}

	/**
	 * Removes armor stand after player leaves it
	 * @param event
	 */
	@EventHandler
	public void onLeaveThrone(EntityDismountEvent event)
	{
		if (!isHub())
			return;

		if (!(event.getEntity() instanceof Item))
			return;

		if (_item == null)
			return;

		if (!event.getEntity().equals(_item))
			return;

		_item.remove();
		_item = null;
	}

	/**
	 * Sets player as peasant
	 * @param event
	 */
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		setPlayerAsPeasant(player);
		updateLobbyKing();
		updateHologram();
	}

	/**
	 * Removes peasant from king if they leave
	 * @param event
	 */
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();
		if (isPeasant(player))
		{
			Peasant peasant = getPeasant(player);
			if (peasant.getKing() != null)
			{
				peasant.getKing().removePeasant(peasant);
			}
			removePeasant(player);
		}
		else if (isKing(player))
		{
			removeKing(player);
		}
		updateLobbyKing();
		updateHologram();
	}

	/**
	 * Spawns the hologram above the castle
	 */
	private void spawnHologram()
	{
		if (!isHub())
			return;

		_hologram = new Hologram(_hologramManager, _hologramLocation, "Mineplex Castle", "Current King: " + F.name("No one") + "!");
		_hologram.start();
	}

	/**
	 * Updates the hologram above the castle
	 */
	private void updateHologram()
	{
		if (!isHub())
		{
			return;
		}
		if (_hologram != null)
		{
			_hologram.stop();
		}

		if (_king != null && _king.getKing().isOnline())
		{
			_hologram = new Hologram(_hologramManager, _hologramLocation, "Mineplex Castle", "Current King: " + F.name(_king.getKing().getName()) + " (" + F.count(_king.amountOfPeasants()) + ")!");
			_hologram.start();
		}
		else
		{
			_hologram = new Hologram(_hologramManager, _hologramLocation, "Mineplex Castle", "Current King: " + F.name("No one") + "!");
			_hologram.start();
		}
		updateWool();
	}

	/**
	 * Updates the hub king
	 * @param oldKing The old king
	 * @param newKing The new king
	 */
	private void updateKing(King oldKing, King newKing)
	{
		setKing(newKing);

		if (!isHub())
			return;

		if (oldKing != null && oldKing.getKing().isOnline())
		{
			// Removes old king from the throne
			if (isInsideCastle(oldKing.getKing().getLocation()))
			{
				oldKing.getKing().teleport(new Location(UtilWorld.getWorld("world"), 0, 78, -31));
				UtilPlayer.message(oldKing.getKing(), F.main("Kingdom", "You are no longer the king of that castle!"));
			}
		}
		if (newKing != null && newKing.getKing().isOnline())
			Bukkit.broadcastMessage(F.main("Kingdom", "" + F.name(newKing.getKing().getName()) + " is the new King of this lobby!"));
	}

	/**
	 * Sets the player as king
	 * @param player The player
	 */
	public void setPlayerAsKing(Player player)
	{
		if (isKing(player))
			return;

		if (isPeasant(player))
			removePeasant(player);

		_kings.put(player, new King(player));

		updateLobbyKing();
		updateHologram();
	}

	/**
	 * Checks if the player is a king
	 * @param player The player
	 * @return true if player is king
	 */
	private boolean isKing(Player player)
	{
		return _kings.containsKey(player);
	}

	/**
	 * Removes the player from the king map
	 * @param player The player
	 */
	private void removeKing(Player player)
	{
		if (!isKing(player))
			return;

		if (getKing().equals(getKing(player)))
			_king = null;

		_kings.get(player).clearPeasants();
		_kings.remove(player);

		if (_item != null)
		{
			if (_item.getPassenger() != null)
			{
				if (_item.getPassenger().equals(player))
				{
					player.leaveVehicle();
				}
			}
		}
		updateLobbyKing();
		updateHologram();
	}

	/**
	 * Gets the king object for the player
	 * @param player The player
	 * @return The king object
	 */
	private King getKing(Player player)
	{
		if (!isKing(player))
			return null;

		return _kings.get(player);
	}

	/**
	 * Sets the player as a peasant
	 * @param player The player
	 */
	public void setPlayerAsPeasant(Player player)
	{
		if (isPeasant(player))
			return;

		if (isKing(player))
			removeKing(player);

		_peasants.put(player, new Peasant(player));
		updateLobbyKing();
		updateHologram();
	}

	/**
	 * Checks if the player is a peasant
	 * @param player The player
	 * @return true if player is peasant
	 */
	private boolean isPeasant(Player player)
	{
		return _peasants.containsKey(player);
	}

	/**
	 * Removes the player from the peasant map
	 * @param player The player
	 */
	private void removePeasant(Player player)
	{
		if (!isPeasant(player))
			return;

		if (getPeasant(player) != null)
		{
			Peasant peasant = getPeasant(player);
			if (peasant.getKing() != null)
			{
				peasant.getKing().removePeasant(peasant);
				peasant.removeKing();
			}
		}
		_peasants.remove(player);

		updateLobbyKing();
		updateHologram();
	}

	/**
	 * Gets the peasant object for the player
	 * @param player The player
	 * @return The peasant object
	 */
	private Peasant getPeasant(Player player)
	{
		if (!isPeasant(player))
			return null;

		return _peasants.get(player);
	}

	/**
	 * Checks if the lobby should update the king (If a king has more peasants than the current king)
	 */
	public void updateLobbyKing()
	{
		if (_kings.size() == 0)
		{
			removeKing();
			return;
		}

		King lobbyKing = null;
		for (King king : _kings.values())
		{
			if (lobbyKing == null)
				lobbyKing = king;
			else if (lobbyKing.amountOfPeasants() < king.amountOfPeasants())
				lobbyKing = king;
		}

		if (_king != null && lobbyKing != null)
		{
			if (_king.amountOfPeasants() == lobbyKing.amountOfPeasants())
				return;

			if (!_king.equals(lobbyKing))
			{
				updateKing(_king, lobbyKing);
			}
		}
		else if (lobbyKing != null)
		{
			setKing(lobbyKing);
		}
	}

	/**
	 * Removes the lobby king
	 */
	private void removeKing()
	{
		_king = null;
		updateHologram();
	}

	/**
	 * Gets the position of a kingdom
	 * @param king The king
	 * @return The position of that kingdom
	 */
	private int getKingPosition(King king)
	{
		if (_king != null)
			if (_king.equals(king))
				return 1;
		int position = 1;
		for (King otherKing : _kings.values())
		{
			if (otherKing.equals(king))
				continue;
			if (otherKing.amountOfPeasants() >= king.amountOfPeasants())
				position++;
		}
		return position;
	}

	/**
	 * Updates the wools above the castle
	 */
	private void updateWool()
	{
		Material material = Material.AIR;
		byte data = (byte) 0;
		if (_king != null)
		{
			King king = getKing();
			PermissionGroup group = _coreClientManager.Get(king.getKing()).getRealOrDisguisedPrimaryGroup();
			material = Material.WOOL;
			data = UtilColor.chatColorToWoolData(group.getColor());
		}
		for (Location location : _woolLocations)
		{
			location.getBlock().setType(material);
			location.getBlock().setData(data);
		}
	}

	/**
	 * Checks if location is inside the castle
	 * @param location
	 * @return
	 */
	public boolean isInsideCastle(Location location)
	{
		if (!isHub())
			return false;

		if (!location.getWorld().equals(_castleLocationA.getWorld()))
		return false;

		return UtilAlg.inBoundingBox(location, _castleLocationA, _castleLocationB);
	}

	/**
	 * Cancels item despawn
	 * @param event
	 */
	@EventHandler
	public void onItemDespawn(UpdateEvent event)
	{
		if (!isHub())
			return;

		if (event.getType() != UpdateType.TICK)
			return;

		if (_item == null)
			return;

		if (!_item.isValid())
		{
			Location loc = _throneLocations[0].clone().add(0.5, 1, 0.5);
			loc.setYaw(90);
			loc.setPitch(0);
			Item item = loc.getWorld().dropItem(loc, new ItemStack(Material.WOOL, 1, (byte) 14));
			item.setPickupDelay(Integer.MAX_VALUE);
			item.setVelocity(new Vector(0, 0, 0));
			item.teleport(loc);
			item.setPassenger(_king.getKing());
			_item = item;
		}
	}

	/**
	 * Stops players from using mounts inside the castle
	 */
	@EventHandler
	public void onMount(GadgetEnableEvent event)
	{
		if (!isHub())
			return;

		if (event.getGadget().getGadgetType() == GadgetType.MOUNT && isInsideCastle(event.getPlayer().getLocation()))
		{
			event.setCancelled(true);
		}
	}

	@EventHandler
	public void onPetSpawn(PetSpawnEvent event)
	{
		if (!isHub())
			return;

		if (isInsideCastle(event.getLocation()))
			event.setCancelled(true);
	}
}