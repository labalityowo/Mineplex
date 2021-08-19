package mineplex.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import mineplex.core.common.DummyEntity;
import mineplex.core.common.MinecraftVersion;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.event.CustomTagEvent;
import mineplex.core.packethandler.IPacketHandler;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.packethandler.PacketHandler;
import mineplex.core.packethandler.PacketVerifier;
import mineplex.core.packethandler.PacketInfo;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import net.minecraft.server.v1_8_R3.DataWatcher;
import net.minecraft.server.v1_8_R3.DataWatcher.WatchableObject;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.EntityArmorStand;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayInUseEntity;
import net.minecraft.server.v1_8_R3.PacketPlayOutAttachEntity;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_8_R3.PacketPlayOutNewAttachEntity;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntity;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class CustomTagFix extends MiniPlugin implements IPacketHandler
{
	private Map<UUID, Map<Integer, Integer[]>> _entityMap = new HashMap<>();
	private Map<UUID, Map<Integer, String>> _entityNameMap = new HashMap<>();
	private Map<UUID, HashMap<Integer, Integer>> _entityRiding = new HashMap<>();

	private Set<UUID> _loggedIn = new HashSet<>();
	private Set<Integer> _ignoreIds = new HashSet<>();

	public CustomTagFix(JavaPlugin plugin, PacketHandler packetHandler)
	{
		super("Custom Tag Fix", plugin);

		packetHandler.addPacketHandler(this, true, PacketPlayOutEntityDestroy.class, PacketPlayOutEntityMetadata.class,
				PacketPlayOutSpawnEntity.class, PacketPlayOutSpawnEntityLiving.class, PacketPlayOutNamedEntitySpawn.class,
				PacketPlayInUseEntity.class, PacketPlayOutAttachEntity.class, PacketPlayOutNewAttachEntity.class);
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		_entityMap.remove(event.getPlayer().getUniqueId());
		_entityNameMap.remove(event.getPlayer().getUniqueId());
		_entityRiding.remove(event.getPlayer().getUniqueId());
		_loggedIn.remove(event.getPlayer().getUniqueId());
	}

	@EventHandler(priority = EventPriority.LOW)
	public void playerJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();

		player.setCustomName("");
		player.setCustomNameVisible(false);
	}

	@EventHandler
	public void cleanMap(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
			return;

		for (Iterator<UUID> iterator = _loggedIn.iterator(); iterator.hasNext(); )
		{
			UUID player = iterator.next();

			if (Bukkit.getPlayer(player) == null)
			{
				iterator.remove();
				_entityMap.remove(player);
				_entityNameMap.remove(player);
				_entityRiding.remove(player);
			}
		}

		if (Bukkit.getServer().getOnlinePlayers().size() < _loggedIn.size())
		{
			System.out.println("PROBLEM - _loggedIn TOOOOOO BIIIIIGGGGG.");
		}
	}


	public void handle(PacketInfo packetInfo)
	{
		if (packetInfo.isCancelled())
			return;

		Packet packet = packetInfo.getPacket();
		Player owner = packetInfo.getPlayer();
		PacketVerifier verifier = packetInfo.getVerifier();

		if (!owner.isOnline())
			// wat
			return;

		if (UtilPlayer.getVersion(owner) != MinecraftVersion.Version1_8)
			return;

		if (!_entityMap.containsKey(owner.getUniqueId()))
		{
			_entityMap.put(owner.getUniqueId(), new HashMap<>());
			_entityNameMap.put(owner.getUniqueId(), new HashMap<>());
			_loggedIn.add(owner.getUniqueId());
		}

		if (packet instanceof PacketPlayOutSpawnEntityLiving)
		{
			PacketPlayOutSpawnEntityLiving spawnPacket = (PacketPlayOutSpawnEntityLiving) packet;

			// Ignore Armor stand packets
			if (spawnPacket.b == EntityType.ARMOR_STAND.getTypeId() || spawnPacket.l == null || spawnPacket.l.c() == null)
			{
				if (spawnPacket.b == EntityType.ARMOR_STAND.getTypeId())
				{
					_ignoreIds.add(spawnPacket.a);
				}

				return;
			}

			for (WatchableObject watchable : (List<WatchableObject>) spawnPacket.l.c())
			{
				if (watchable.a() == 3 && watchable.b() instanceof Byte && ((Byte) watchable.b()) == 1)
				{
					if (_entityMap.get(owner.getUniqueId()).containsKey(spawnPacket.a))
					{
						Integer[] ids = _entityMap.get(owner.getUniqueId()).get(spawnPacket.a);
						int[] newIds = new int[ids.length];

						for (int a = 0; a < ids.length; a++)
						{
							newIds[a] = ids[a];
						}

						UtilPlayer.sendPacket(owner, new PacketPlayOutEntityDestroy(newIds));

						_entityNameMap.get(owner.getUniqueId()).remove(spawnPacket.a);
						_entityMap.get(owner.getUniqueId()).remove(spawnPacket.a);
					}

					final String entityName = spawnPacket.l.getString(2);

					if (entityName.isEmpty())
					{
						return;
					}

					Integer[] ids = new Integer[]
							{
									UtilEnt.getNewEntityId(),
									UtilEnt.getNewEntityId()
							};

					_entityNameMap.get(owner.getUniqueId()).put(spawnPacket.a, entityName);
					_entityMap.get(owner.getUniqueId()).put(spawnPacket.a, ids);

					sendProtocolPackets(owner, spawnPacket.a, entityName, verifier, true, ids);
					break;
				}
			}
		}
		else if (packet instanceof PacketPlayOutNamedEntitySpawn)
		{
			PacketPlayOutNamedEntitySpawn spawnPacket = (PacketPlayOutNamedEntitySpawn) packet;

			for (WatchableObject watchable : (List<WatchableObject>) spawnPacket.i.c())
			{
				if (watchable.a() == 3 && watchable.b() instanceof Byte && ((Byte) watchable.b()) == 1)
				{
					if (_entityMap.get(owner.getUniqueId()).containsKey(spawnPacket.a))
					{
						Integer[] ids = _entityMap.get(owner.getUniqueId()).get(spawnPacket.a);

						int[] newIds = new int[ids.length];

						for (int a = 0; a < ids.length; a++)
						{
							newIds[a] = ids[a];
						}

						UtilPlayer.sendPacket(owner, new PacketPlayOutEntityDestroy(newIds));

						_entityNameMap.get(owner.getUniqueId()).remove(spawnPacket.a);
						_entityMap.get(owner.getUniqueId()).remove(spawnPacket.a);
					}

					final String entityName = spawnPacket.i.getString(2);

					if (entityName.isEmpty())
					{
						return;
					}

					Integer[] ids = new Integer[]
							{
									UtilEnt.getNewEntityId(),
									UtilEnt.getNewEntityId()
							};

					_entityNameMap.get(owner.getUniqueId()).put(spawnPacket.a, entityName);
					_entityMap.get(owner.getUniqueId()).put(spawnPacket.a, ids);

					sendProtocolPackets(owner, spawnPacket.a, entityName, verifier, true, ids);
					break;
				}
			}
		}
		else if (packet instanceof PacketPlayOutEntityMetadata)
		{
			PacketPlayOutEntityMetadata metaPacket = (PacketPlayOutEntityMetadata) packet;

			if (metaPacket.a != 777777 && !_ignoreIds.contains(metaPacket.a) && metaPacket.a != owner.getEntityId())
			{
				boolean isDisplaying = _entityMap.get(owner.getUniqueId()).containsKey(metaPacket.a);
				String currentName = _entityNameMap.get(owner.getUniqueId()).get(metaPacket.a);

				if (currentName == null)
				{
					currentName = "";
				}

				String newName = currentName;
				boolean displayName = isDisplaying;

				for (WatchableObject watchable : (List<WatchableObject>) metaPacket.b)
				{
					if (watchable.a() == 3 && watchable.b() instanceof Byte)
					{
						displayName = ((Byte) watchable.b()) == 1;
					}

					if (watchable.a() == 2 && watchable.b() instanceof String)
					{
						newName = (String) watchable.b();
					}
				}

				// If the name has changed and the name should be showing, or the name display status has changed.
				if ((!newName.equals(currentName) && displayName) || displayName != isDisplaying)
				{
					// If name is still being displayed
					if (displayName)
					{
						Integer[] newId;

						if (isDisplaying) // Sending metadata
						{
							newId = _entityMap.get(owner.getUniqueId()).get(metaPacket.a);
						}
						else
						// Spawning new entity
						{
							newId = new Integer[]
									{
											UtilEnt.getNewEntityId(),
											UtilEnt.getNewEntityId()
									};

							_entityMap.get(owner.getUniqueId()).put(metaPacket.a, newId);
						}

						_entityNameMap.get(owner.getUniqueId()).put(metaPacket.a, newName);
						sendProtocolPackets(owner, metaPacket.a, newName, verifier, !isDisplaying, newId);
					}
					else
					{ // Lets delete it
						Integer[] ids = _entityMap.get(owner.getUniqueId()).get(metaPacket.a);
						int[] newIds = new int[ids.length];

						for (int a = 0; a < ids.length; a++)
						{
							newIds[a] = ids[a];
						}

						verifier.bypassProcess(new PacketPlayOutEntityDestroy(newIds));

						_entityMap.get(owner.getUniqueId()).remove(metaPacket.a);
						_entityNameMap.get(owner.getUniqueId()).remove(metaPacket.a);
					}
				}
			}
		}
		else if (packet instanceof PacketPlayOutEntityDestroy)
		{
			try
			{
				for (int id : ((PacketPlayOutEntityDestroy) packet).a)
				{
					if (_entityMap.get(owner.getUniqueId()).containsKey(id))
					{
						Integer[] ids = _entityMap.get(owner.getUniqueId()).get(id);
						int[] newIds = new int[ids.length];

						for (int a = 0; a < ids.length; a++)
						{
							newIds[a] = ids[a];
						}

						UtilPlayer.sendPacket(owner, new PacketPlayOutEntityDestroy(newIds));
						_entityMap.get(owner.getUniqueId()).remove(id);
						_entityNameMap.get(owner.getUniqueId()).remove(id);
					}
				}
			}
			catch (Exception exception)
			{
				exception.printStackTrace();
			}
		}
		else if (packet instanceof PacketPlayOutSpawnEntity)
		{
			PacketPlayOutSpawnEntity spawnPacket = (PacketPlayOutSpawnEntity) packet;
			if (spawnPacket.j == 78) // Armor Stand Object ID
			{
				_ignoreIds.add(spawnPacket.a);
			}
		}
		else if (packet instanceof PacketPlayInUseEntity)
		{
			PacketPlayInUseEntity usePacket = (PacketPlayInUseEntity) packet;

			loop:

			for (Entry<Integer, Integer[]> entry : _entityMap.get(owner.getUniqueId()).entrySet())
			{
				for (int id : entry.getValue())
				{
					if (id == usePacket.a)
					{
						PacketPlayInUseEntity newPacket = new PacketPlayInUseEntity();
						newPacket.a = entry.getKey();
						newPacket.action = usePacket.action;
						newPacket.c = usePacket.c;

						{
							((CraftPlayer) owner).getHandle().playerConnection.a(newPacket);
						}

						break loop;
					}
				}
			}
		}
		else if (packet instanceof PacketPlayOutAttachEntity || packet instanceof PacketPlayOutNewAttachEntity)
		{
			int vech = -1;
			int rider = -1;

			if (packet instanceof PacketPlayOutAttachEntity)
			{
				PacketPlayOutAttachEntity attachPacket = (PacketPlayOutAttachEntity) packet;
				vech = attachPacket.b;
				rider = attachPacket.c;
			}
			else if (packet instanceof PacketPlayOutNewAttachEntity)
			{
				PacketPlayOutNewAttachEntity attachPacket = (PacketPlayOutNewAttachEntity) packet;
				vech = attachPacket.a;

				if (attachPacket.b.length > 0)
					rider = attachPacket.b[0];
			}

			// c = rider, b = ridden
			// When detaching, c is sent, b is -1

			// If this attach packet is for a player that has the fix
			// If the attach packet isn't ordained by me
			if (!_entityMap.containsKey(owner.getUniqueId()))
			{
				return;
			}

			if (!_entityRiding.containsKey(owner.getUniqueId()))
			{
				_entityRiding.put(owner.getUniqueId(), new HashMap<Integer, Integer>());
			}

			int vehicleId = -1;

			if (_entityRiding.get(owner.getUniqueId()).containsKey(vech))
			{
				vehicleId = _entityRiding.get(owner.getUniqueId()).get(vech);
			}

			if (rider == -1 && _entityMap.get(owner.getUniqueId()).containsKey(vehicleId))
			{
				Integer[] ids = _entityMap.get(owner.getUniqueId()).get(vehicleId);

				_entityRiding.get(owner.getUniqueId()).remove(vech);

				sendProtocolPackets(owner, vehicleId, _entityNameMap.get(owner.getUniqueId()).get(vehicleId), verifier, true,
						ids);
			}
			else
			{
				Integer[] ids = _entityMap.get(owner.getUniqueId()).get(rider);

				if (ids != null && ids[1] != vech)
				{
					_entityRiding.get(owner.getUniqueId()).put(vech, rider);

					int[] newIds = new int[ids.length];

					for (int a = 0; a < ids.length; a++)
					{
						newIds[a] = ids[a];
					}

					UtilPlayer.sendPacket(owner, new PacketPlayOutEntityDestroy(newIds));
				}
			}
		}
	}

	private void sendProtocolPackets(final Player owner, final int entityId, String entityName, final PacketVerifier packetList,
									 final boolean newPacket, final Integer[] entityIds)
	{
		CustomTagEvent event = new CustomTagEvent(owner, entityId, entityName);
		_plugin.getServer().getPluginManager().callEvent(event);
		final String finalEntityName = event.getCustomName();

		Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(getPlugin(), new Runnable()
		{
			public void run()
			{
				DataWatcher watcher = new DataWatcher(new DummyEntity(((CraftWorld) owner.getWorld()).getHandle()));

				watcher.a(0, (byte) 32, Entity.META_ENTITYDATA, (byte) 32); // Invisible
				watcher.a(1, Short.valueOf((short) 300), Entity.META_AIR, 0);
				watcher.a(2, finalEntityName, Entity.META_CUSTOMNAME, finalEntityName);
				watcher.a(3, (byte) 1, Entity.META_CUSTOMNAME_VISIBLE, true);
				watcher.a(10, (byte) 16, EntityArmorStand.META_ARMOR_OPTION, (byte) 16); // Small

				if (newPacket)
				{
					{
						DataWatcher squidWatcher = new DataWatcher(new DummyEntity(((CraftWorld) owner.getWorld()).getHandle()));
						squidWatcher.a(0, (byte) 32, Entity.META_ENTITYDATA, (byte) 32);

						PacketPlayOutSpawnEntityLiving spawnPacket = new PacketPlayOutSpawnEntityLiving();
						spawnPacket.a = entityIds[1];
						spawnPacket.b = (byte) EntityType.SQUID.getTypeId();
						spawnPacket.c = owner.getLocation().getBlockX() * 32;
						spawnPacket.d = -150;
						spawnPacket.e = owner.getLocation().getBlockZ() * 32;

						spawnPacket.l = squidWatcher;
						spawnPacket.uuid = UUID.randomUUID();

						UtilPlayer.sendPacket(owner, spawnPacket);

						if (UtilPlayer.getVersion(owner).atOrAbove(MinecraftVersion.Version1_9))
						{
							UtilPlayer.sendPacket(owner, new PacketPlayOutNewAttachEntity(entityId, new int[]
									{
											entityIds[1]
									}));
						}
						else
						{
							PacketPlayOutAttachEntity vehiclePacket = new PacketPlayOutAttachEntity();
							vehiclePacket.a = 0;
							vehiclePacket.b = spawnPacket.a;
							vehiclePacket.c = entityId;

							UtilPlayer.sendPacket(owner, vehiclePacket);
						}
					}

					PacketPlayOutSpawnEntityLiving spawnPacket = new PacketPlayOutSpawnEntityLiving();
					spawnPacket.a = entityIds[0];
					spawnPacket.b = (byte) 30;
					spawnPacket.c = owner.getLocation().getBlockX() * 32;
					spawnPacket.d = -150;
					spawnPacket.e = owner.getLocation().getBlockZ() * 32;

					spawnPacket.l = watcher;
					spawnPacket.uuid = UUID.randomUUID();

					UtilPlayer.sendPacket(owner, spawnPacket);

					if (UtilPlayer.getVersion(owner).atOrAbove(MinecraftVersion.Version1_9))
					{
						UtilPlayer.sendPacket(owner, new PacketPlayOutNewAttachEntity(entityIds[1], new int[]
								{
										entityIds[0]
								}));
					}
					else
					{
						PacketPlayOutAttachEntity vehiclePacket = new PacketPlayOutAttachEntity();
						vehiclePacket.a = 0;
						vehiclePacket.b = entityIds[0];
						vehiclePacket.c = entityIds[1];

						UtilPlayer.sendPacket(owner, vehiclePacket);
					}
				}
				else
				{
					PacketPlayOutEntityMetadata entityMetadata = new PacketPlayOutEntityMetadata(entityIds[0], watcher, true);

					packetList.bypassProcess(entityMetadata);
				}
			}
		});
	}
}
