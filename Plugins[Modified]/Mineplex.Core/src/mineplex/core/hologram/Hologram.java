package mineplex.core.hologram;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import mineplex.core.common.MinecraftVersion;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilPlayer;
import net.minecraft.server.v1_8_R3.DataWatcher;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntity;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;

/**
 * Floating text object with interaction and entity follow capabilities.
 */
public class Hologram {

	private Packet _destroy1_8;

	/**
	 * 1.7 packets uses both EntityIDs while 1.8 uses only the first.
	 */
	private List<Integer> _entityIds = new ArrayList<>();
	private Entity _followEntity;
	private HologramManager _hologramManager;
	private String[] _hologramText = new String[0];

	/**
	 * Keeps track of the holograms movements. This fixes offset that
	 * occasionally happens when moving a hologram around.
	 */
	private Vector _lastMovement;
	private Location _location;
	private boolean _makeDestroyPackets = true;
	private boolean _makeSpawnPackets = true;
	private Packet[] _packets1_8;
	private Packet[] _packets1_9;
	private Set<UUID> _playersInList = new HashSet<>();
	private List<Player> _playersTracking = new ArrayList<>();
	private boolean _removeEntityDeath;
	private HologramTarget _target = HologramTarget.BLACKLIST;
	private int _viewDistance = 70;
	protected Vector relativeToEntity;
	private boolean _hideBoundingBox;
	private HologramInteraction _interaction;

	private long _maxLifetime = -1;
	private long _startTime;

	/**
	 * Construct a standard hologram.
	 *
	 * @param hologramManager  The hologram manager.
	 * @param location         The location at which to display the hologram.
	 * @param text             An array of text lines which the hologram should display.
	 */
	public Hologram(HologramManager hologramManager, Location location, String... text)
	{
		this(hologramManager, location, false, -1L, text);
	}

	/**
	 * Construct a hologram with a specified bounding box.
	 *
	 * @param hologramManager  The hologram manager.
	 * @param location         The location at which to display the hologram.
	 * @param hideBoundingBox  Whether to hide the bounding box of the hologram.
	 * @param text             An array of text lines which the hologram should display.
	 */
	public Hologram(HologramManager hologramManager, Location location, boolean hideBoundingBox, String... text)
	{
		this(hologramManager, location, hideBoundingBox, -1L, text);
	}

	/**
	 * Construct a hologram with a limited lifetime.
	 *
	 * @param hologramManager  The hologram manager.
	 * @param location         The location at which to display the hologram.
	 * @param hideBoundingBox  Whether to hide the bounding box of the hologram.
	 * @param maxLifetime      The max lifetime of the hologram, specified in milliseconds.
	 * @param text             An array of text lines which the hologram should display.
	 */
	public Hologram(HologramManager hologramManager, Location location, boolean hideBoundingBox, long maxLifetime, String... text)
	{
		_hologramManager = hologramManager;
		_location = location.clone();
		_maxLifetime = maxLifetime;
		_hideBoundingBox = hideBoundingBox;
		setText(text);
	}

	/**
	 * Set the interaction handler for the hologram.
	 *
	 * @param interact The handler.
	 *
	 * @return the original hologram object.
	 */
	public Hologram setInteraction(HologramInteraction interact)
	{
		_interaction = interact;
		return this;
	}

	public HologramInteraction getInteraction() {
		return _interaction;
	}

	/**
	 * Adds the player to the Hologram to be effected by Whitelist or Blacklist
	 */
	public Hologram addPlayer(Player player)
	{
		return addPlayer(player.getUniqueId());
	}

	/**
	 * Adds the player to the Hologram to be effected by Whitelist or Blacklist
	 */
	public Hologram addPlayer(UUID player)
	{
		_playersInList.add(player);
		return this;
	}

	/**
	 * Adds a collection of players to the Hologram to be effected by Whitelist or Blacklist
	 */
	public Hologram addPlayers(Collection<Player> players)
	{
		players.forEach(this::addPlayer);
		return this;
	}

	/**
	 * Hides the bounding box for the hologram. <br>
	 *
	 * <b>Warning! Bounding box if hidden will hide holograms for 1.8 to 1.8.2</b>
	 * 
	 * @return the original hologram object.
	 */
	public Hologram setHideBoundingBox()
	{
		_hideBoundingBox = true;
		return this;
	}

	/**
	 * @return if there is a player entry in the holograms whitelist/blacklist.
	 */
	public boolean containsPlayer(Player player)
	{
		return _playersInList.contains(player.getUniqueId());
	}

	/**
	 * Generates a packet to destroy the hologram client-side.
	 *
	 * @return the packet.
	 */
	protected Packet getDestroyPacket()
	{
		if (_makeDestroyPackets)
		{
			makeDestroyPacket();
			_makeDestroyPackets = false;
		}

		return _destroy1_8;
	}

	/**
	 * @return the entity that this hologram is currently following.
	 */
	public Entity getEntityFollowing()
	{
		return _followEntity;
	}

	/**
	 * @return The functionality that is currently being used for the object's internal
	 *         player list.
	 *         {@link HologramTarget#WHITELIST} = Only people added can see the hologram
	 *         {@link HologramTarget#BLACKLIST} = Anyone but people added can see the hologram
	 */
	public HologramTarget getHologramTarget()
	{
		return _target;
	}

	/**
	 * @return the current location of the hologram.
	 */
	public Location getLocation()
	{
		return _location.clone();
	}

	/**
	 * @return A list of players that can currently see the hologram.
	 */
	protected ArrayList<Player> getNearbyPlayers()
	{
		ArrayList<Player> nearbyPlayers = new ArrayList<>();

		for (Player player : getLocation().getWorld().getPlayers())
		{
			if (isVisible(player))
			{
				nearbyPlayers.add(player);
			}
		}

		return nearbyPlayers;
	}

	/**
	 * @return The list of players that are in the holograms whitelist or blacklist.
	 */
	protected List<Player> getPlayersTracking()
	{
		return _playersTracking;
	}

	/**
	 * Generates hologram spawn packets if they have not been created already.
	 */
	protected void checkSpawnPackets()
	{
		if (_makeSpawnPackets)
		{
			makeSpawnPackets();
			_makeSpawnPackets = false;
		}
	}

	/**
	 * @return the current text being displayed by the hologram.
	 */
	public String[] getText()
	{
		// We reverse it again as the hologram would otherwise display the text
		// from the bottom row to the top row
		String[] reversed = new String[_hologramText.length];

		for (int i = 0; i < reversed.length; i++)
		{
			reversed[i] = _hologramText[reversed.length - (i + 1)];
		}

		return reversed;
	}

	/**
	 * @return the view distance the hologram is viewable from. Default is 70
	 */
	public int getViewDistance()
	{
		return _viewDistance;
	}

	/**
	 * @return Is the hologram holograming?
	 */
	public boolean isInUse()
	{
		return _lastMovement != null;
	}

	/**
	 * @return whether to delete the hologram when the entity it is following dies.
	 */
	public boolean isRemoveOnEntityDeath()
	{
		return _removeEntityDeath;
	}

	/**
	 * Determines whether the hologram is visible to a player based on view distance, whitelist /
	 * blacklist, and current world.
	 *
	 * @param player The player to check.
	 *
	 * @return whether the hologram is visible to the player.
	 */
	public boolean isVisible(Player player)
	{
		if (getLocation().getWorld() == player.getWorld())
		{
			if ((getHologramTarget() == HologramTarget.WHITELIST) == containsPlayer(player))
			{
				if (getLocation().distanceSquared(player.getLocation()) < getViewDistance() * getViewDistance())
				{
					return true;
				}
			}
		}

		return false;
	}

	/**
	 * Generates a packet to destroy the hologram client-side.
	 */
	private void makeDestroyPacket()
	{
		int[] entityIds1_8 = new int[_entityIds.size()];

		for (int i = 0; i < _entityIds.size(); i++)
		{
			entityIds1_8[i] = _entityIds.get(i);
		}

		_destroy1_8 = new PacketPlayOutEntityDestroy(entityIds1_8);
	}

	/**
	 * Generates spawn packets for the hologram.
	 */
	private void makeSpawnPackets()
	{
		_packets1_8 = new Packet[_hologramText.length];
		_packets1_9 = new Packet[_hologramText.length];

		if (_entityIds.size() < _hologramText.length)
		{
			_makeDestroyPackets = true;

			for (int i = _entityIds.size(); i < _hologramText.length; i++)
			{
				_entityIds.add(UtilEnt.getNewEntityId());
			}
		}
		else
		{
			_makeDestroyPackets = true;

			while (_entityIds.size() > _hologramText.length)
			{
				_entityIds.remove(_hologramText.length);
			}
		}
		for (int textRow = 0; textRow < _hologramText.length; textRow++)
		{
			PacketPlayOutSpawnEntityLiving packet1_8 = makeSpawnPacket1_8(textRow, _entityIds.get(textRow), _hologramText[textRow]);
			PacketPlayOutSpawnEntityLiving packet1_9 = makeSpawnPacket1_9(textRow, _entityIds.get(textRow), _hologramText[textRow]);

			_packets1_8[textRow] = packet1_8;
			_packets1_9[textRow] = packet1_9;
		}
	}

	/**
	 * Used for sending 1.9 clients holograms with no bounding boxes.
	 */
	private PacketPlayOutSpawnEntityLiving makeSpawnPacket1_9(int textRow, int entityId, String lineOfText)
	{
		PacketPlayOutSpawnEntityLiving packet = makeSpawnPacket1_8(textRow, entityId, lineOfText);

		if (_hideBoundingBox)
		{
			DataWatcher watcher = packet.l;
			packet.d = (int) ((getLocation().getY() + ((double) textRow * 0.31)) * 32);
			watcher.a(10, (byte) 16, EntityArmorStand.META_ARMOR_OPTION, (byte) 16);
		}

		return packet;
	}

	/**
	 * Used for sending 1.8 clients holograms.
	 */
	private PacketPlayOutSpawnEntityLiving makeSpawnPacket1_8(int textRow, int entityId, String lineOfText)
	{
		PacketPlayOutSpawnEntityLiving packet = new PacketPlayOutSpawnEntityLiving();
		DataWatcher watcher = new DataWatcher(null);

		packet.a = entityId;
		packet.b = 30;
		packet.c = (int) (getLocation().getX() * 32);
		packet.d = (int) ((getLocation().getY() - 2.1 + ((double) textRow * 0.31)) * 32);
		packet.e = (int) (getLocation().getZ() * 32);
		packet.l = watcher;
		packet.uuid = UUID.randomUUID();

		// Setup datawatcher for armor stand
		watcher.a(0, (byte) 32, EntityArmorStand.META_ENTITYDATA, (byte) 32);
		watcher.a(2, lineOfText, EntityArmorStand.META_CUSTOMNAME, lineOfText);
		watcher.a(3, (byte) 1, EntityArmorStand.META_CUSTOMNAME_VISIBLE, true);

		return packet;
	}

	/**
	 * Removes a player from the Hologram so they are no longer effected by
	 * whitelist or blacklist.
	 *
	 * @param player The player to remove.
	 *
	 * @return the original hologram object.
	 */
	public Hologram removePlayer(Player player)
	{
		return removePlayer(player.getUniqueId());
	}

	/**
	 * Removes the player from the Hologram so they are no longer effected by
	 * whitelist or blacklist
	 *
	 * @param player The player to remove.
	 *
	 * @return the original hologram object.
	 */
	public Hologram removePlayer(UUID player)
	{
		_playersInList.remove(player);
		return this;
	}

	/**
	 * Sets an entity to which the hologram will remain relative to in position.
	 *
	 * @param entityToFollow the entity which to follow.
	 *
	 * @return the original hologram object.
	 */
	public Hologram setFollowEntity(Entity entityToFollow)
	{
		_followEntity = entityToFollow;

		if (entityToFollow != null)
		{
			relativeToEntity = _location.clone().subtract(entityToFollow.getLocation()).toVector();
		}

		return this;
	}

	/**
	 * Set how the hologram's internal player list is used.
	 *
	 * @param newTarget The target which defines how the list is used.
	 *                  {@link HologramTarget#WHITELIST} = Only people added can see the hologram
	 *                  {@link HologramTarget#BLACKLIST} = Anyone but people added can see the hologram
	 *
	 * @retuen the original hologram object.
	 */
	public Hologram setHologramTarget(HologramTarget newTarget)
	{
		_target = newTarget;
		return this;
	}

	/**
	 * Change the location of the hologram.
	 *
	 * @param newLocation the location to which to teleport the hologram.
	 *
	 * @return the original hologram object.
	 */
	public Hologram setLocation(Location newLocation)
	{
		_makeSpawnPackets = true;

		Location oldLocation = getLocation();
		_location = newLocation.clone();

		if (getEntityFollowing() != null)
		{
			relativeToEntity = _location.clone().subtract(getEntityFollowing().getLocation()).toVector();
		}

		if (isInUse())
		{
			ArrayList<Player> canSee = getNearbyPlayers();
			Iterator<Player> itel = _playersTracking.iterator();

			while (itel.hasNext())
			{
				Player player = itel.next();
				if (!canSee.contains(player))
				{
					itel.remove();

					if (player.getWorld() == getLocation().getWorld())
					{
						UtilPlayer.sendPacket(player, getDestroyPacket());
					}
				}
			}

			itel = canSee.iterator();
			checkSpawnPackets();

			while (itel.hasNext())
			{
				Player player = itel.next();

				if (!_playersTracking.contains(player))
				{
					_playersTracking.add(player);
					itel.remove();

					UtilPlayer.sendPacket(player, (UtilPlayer.getVersion(player).atOrAbove(MinecraftVersion.Version1_9)) ? _packets1_9 : _packets1_8);
				}
			}

			if (!canSee.isEmpty())
			{
				_lastMovement.add(new Vector(newLocation.getX() - oldLocation.getX(),
						newLocation.getY() - oldLocation.getY(), newLocation.getZ() - oldLocation.getZ()));

				int x = (int) Math.floor(32 * _lastMovement.getX());
				int y = (int) Math.floor(32 * _lastMovement.getY());
				int z = (int) Math.floor(32 * _lastMovement.getZ());

				Packet[] packets1_8 = new Packet[_hologramText.length];
				Packet[] packets1_9 = new Packet[_hologramText.length];

				int i = 0;

				// Generate packets for 1.9 clients
				x = (int) Math.floor(32 * newLocation.getX());
				z = (int) Math.floor(32 * newLocation.getZ());

				_lastMovement = new Vector(newLocation.getX() - (x / 32D), 0, newLocation.getZ() - (z / 32D));

				for (Integer entityId : _entityIds)
				{
					PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport();
					teleportPacket.a = entityId;
					teleportPacket.b = x;
					teleportPacket.c = (int) Math.floor((oldLocation.getY() + (-2.1) + ((double) i * 0.31)) * 32);
					teleportPacket.d = z;

					packets1_9[i] = teleportPacket;

					i++;
				}

				i = 0;

				// Generate move packets for 1.8 clients if the move is small enough.
				if (x >= -128 && x <= 127 && y >= -128 && y <= 127 && z >= -128 && z <= 127)
				{
					_lastMovement.subtract(new Vector(x / 32D, y / 32D, z / 32D));

					for (Integer entityId : _entityIds)
					{
						PacketPlayOutEntity.PacketPlayOutRelEntityMove relMove = new PacketPlayOutEntity.PacketPlayOutRelEntityMove();

						relMove.a = entityId;
						relMove.b = (byte) x;
						relMove.c = (byte) y;
						relMove.d = (byte) z;

						packets1_8[i] = relMove;
						i++;
					}
				}
				else // Use teleport packets
				{
					packets1_8 = packets1_9;
				}

				for (Player player : canSee)
				{
					if (UtilPlayer.getVersion(player).atOrAbove(MinecraftVersion.Version1_9))
					{
						for (Packet packet : packets1_9)
						{
							UtilPlayer.sendPacket(player, packet);
						}
					}
					else
					{
						for (Packet packet : packets1_8)
						{
							UtilPlayer.sendPacket(player, packet);
						}
					}
				}
			}
		}

		return this;
	}

	/**
	 * @return the time at which the hologram was last started.
	 */
	public long getStartTime()
	{
		return _startTime;
	}

	/**
	 * @return the max time after the hologram was started for which it will live.
	 */
	public long getMaxLifetime()
	{
		return _maxLifetime;
	}

	/**
	 * Set the hologram to stop when the entity it is following dies.
	 *
	 * @return the original hologram object.
	 */
	public Hologram setRemoveOnEntityDeath()
	{
		_removeEntityDeath = true;
		return this;
	}

	/**
	 * @param entityId an entity id.
	 *
	 * @return whether the entity ID is represented by this hologram object.
	 */
	public boolean isEntityId(int entityId)
	{
		return _entityIds.contains(entityId);
	}

	/**
	 * Set the hologram text
	 *
	 * @param newLines array of text lines for the hologram to display.
	 *
	 * @return the original hologram object.
	 */
	public Hologram setText(String... newLines)
	{
		String[] newText = new String[newLines.length];

		for (int i = 0; i < newText.length; i++)
		{
			newText[i] = newLines[newText.length - (i + 1)];
		}

		if (Arrays.equals(newText, _hologramText))
		{
			return this;
		}

		if (isInUse())
		{
			int[] destroy1_8 = new int[0];

			ArrayList<Packet> packets1_8 = new ArrayList<>();
			ArrayList<Packet> packets1_9 = new ArrayList<>();

			if (_hologramText.length != newText.length)
			{
				_makeDestroyPackets = true;
			}

			for (int i = 0; i < Math.max(_hologramText.length, newText.length); i++)
			{
				if (i >= _hologramText.length) // If more lines than previously
				{
					// Add entity id and send spawn packets
					// You add a entity id because the new hologram needs
					int entityId = UtilEnt.getNewEntityId();
					_entityIds.add(entityId);

					packets1_8.add(makeSpawnPacket1_8(i, entityId, newText[i]));
					packets1_9.add(makeSpawnPacket1_9(i, entityId, newText[i]));
				}
				else if (i >= newText.length) // If less lines than previously
				{
					// Remove entity id and send destroy packets
					Integer entityId = _entityIds.remove(newText.length);

					destroy1_8 = Arrays.copyOf(destroy1_8, destroy1_8.length + 1);
					destroy1_8[destroy1_8.length - 1] = entityId;
				}
				else if (!newText[i].equals(_hologramText[i]))
				{
					// Send update metadata packets
					Integer entityId = _entityIds.get(i);

					DataWatcher watcher1_8 = new DataWatcher(null);
					DataWatcher watcher1_9 = new DataWatcher(null);

					{
						watcher1_8.a(0, (byte) 32, EntityArmorStand.META_ENTITYDATA, (byte) 32);
						watcher1_8.a(2, newText[i], EntityArmorStand.META_CUSTOMNAME, newText[i]);
						watcher1_8.a(3, (byte) 1, EntityArmorStand.META_CUSTOMNAME_VISIBLE, true);
					}

					{
						watcher1_9.a(0, (byte) 32, EntityArmorStand.META_ENTITYDATA, (byte) 32);
						watcher1_9.a(2, newText[i], EntityArmorStand.META_CUSTOMNAME, newText[i]);
						watcher1_9.a(3, (byte) 1, EntityArmorStand.META_CUSTOMNAME_VISIBLE, true);

						if (_hideBoundingBox)
						{
							watcher1_9.a(10, (byte) 16, EntityArmorStand.META_ARMOR_OPTION, (byte) 16);
						}
					}

					packets1_8.add(new PacketPlayOutEntityMetadata(entityId, watcher1_8, true));
					packets1_9.add(new PacketPlayOutEntityMetadata(entityId, watcher1_9, true));
				}
			}

			if (destroy1_8.length > 0)
			{
				packets1_8.add(new PacketPlayOutEntityDestroy(destroy1_8));
				packets1_9.add(new PacketPlayOutEntityDestroy(destroy1_8));
			}

			for (Player player : _playersTracking)
			{
				List<Packet> packets = (UtilPlayer.getVersion(player).atOrAbove(MinecraftVersion.Version1_9)) ? packets1_9 : packets1_8;

				for (Packet packet : packets)
				{
					UtilPlayer.sendPacket(player, packet);
				}
			}
		}

		_hologramText = newText;
		makeSpawnPackets();

		return this;
	}

	/**
	 * Set the distance the hologram is viewable from. Default is 70
	 *
	 * @param newDistance The distance in blocks.
	 *
	 * @return the original hologram object.
	 */
	public Hologram setViewDistance(int newDistance)
	{
		_viewDistance = newDistance;
		return setLocation(getLocation());
	}

	/**
	 * Start the hologram, displaying it to players.
	 *
	 * @return the original hologram object.
	 */
	public Hologram start()
	{
		if (!isInUse())
		{
			_startTime = System.currentTimeMillis();
			
			_hologramManager.addHologram(this);
			_playersTracking.addAll(getNearbyPlayers());

			sendPackets();

			_lastMovement = new Vector();
		}

		return this;
	}

	/**
	 * Sends hologram spawn packets to players.
	 */
	private void sendPackets()
	{
		checkSpawnPackets();

		for (Player player : _playersTracking)
		{
			UtilPlayer.sendPacket(player, (UtilPlayer.getVersion(player).atOrAbove(MinecraftVersion.Version1_9)) ? _packets1_9 : _packets1_8);
		}
	}

	/**
	 * Generates spawn packets based on minecraft version.
	 *
	 * @param player The player for which to generate the packets.
	 *
	 * @return the list of packets generated.
	 */
	public Packet[] getSpawnPackets(Player player)
	{
		checkSpawnPackets();

		return (UtilPlayer.getVersion(player).atOrAbove(MinecraftVersion.Version1_9)) ? _packets1_9 : _packets1_8;
	}

	/**
	 * Stop the hologram, effectively destroying it once
	 * garbage collection has occurred.
	 *
	 * @return the original hologram object.
	 */
	public Hologram stop()
	{
		if (isInUse())
		{
			_hologramManager.removeHologram(this);

			for (Player player : _playersTracking)
			{
				UtilPlayer.sendPacket(player, getDestroyPacket());
			}

			_playersTracking.clear();
			_lastMovement = null;
		}

		return this;
	}

	/**
	 * Enum defining to whom the hologram is displayed.
	 */
	public enum HologramTarget
	{
		BLACKLIST, WHITELIST
	}
}
