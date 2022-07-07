package mineplex.core.disguise;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;

import net.minecraft.server.v1_8_R3.BlockBed;
import net.minecraft.server.v1_8_R3.BlockPosition;
import net.minecraft.server.v1_8_R3.Blocks;
import net.minecraft.server.v1_8_R3.Chunk;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.EntityTrackerEntry;
import net.minecraft.server.v1_8_R3.EnumDirection;
import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutBed;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntity;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntity.PacketPlayOutEntityLook;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntity.PacketPlayOutRelEntityMove;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntity.PacketPlayOutRelEntityMoveLook;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityTeleport;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityVelocity;
import net.minecraft.server.v1_8_R3.PacketPlayOutMapChunk;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntity;
import net.minecraft.server.v1_8_R3.PacketPlayOutSpawnEntityLiving;
import net.minecraft.server.v1_8_R3.PacketPlayOutUpdateAttributes;
import net.minecraft.server.v1_8_R3.PacketPlayOutVehicleMove;
import net.minecraft.server.v1_8_R3.WorldServer;
import net.minecraft.server.v1_8_R3.WorldSettings;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import com.mineplex.spigot.ChunkAddEntityEvent;

import mineplex.core.MiniPlugin;
import mineplex.core.PlayerSelector;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.common.util.UtilLambda;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTasks;
import mineplex.core.disguise.disguises.DisguiseBase;
import mineplex.core.disguise.disguises.DisguiseBlock;
import mineplex.core.disguise.disguises.DisguiseInsentient;
import mineplex.core.disguise.disguises.DisguiseLiving;
import mineplex.core.disguise.disguises.DisguisePlayer;
import mineplex.core.disguise.disguises.DisguiseSquid;
import mineplex.core.packethandler.IPacketHandler;
import mineplex.core.packethandler.PacketHandler;
import mineplex.core.packethandler.PacketInfo;
import mineplex.core.packethandler.PacketVerifier;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

/*
 * notes: rabbit jump has been removed (PacketPlayOutEntityStatus) because it didn't work for 1.9+ anyways
 *
 * contact samczsun before you make any major changes
 */
@ReflectivelyCreateMiniPlugin
public class DisguiseManager extends MiniPlugin implements IPacketHandler
{

	private static final String HIDE_PLAYER_NAME_TEAM = "hiddenNPCS";

	// A map of entityids which are disguised to their respective disguises
	private final Map<Integer, LinkedList<DisguiseBase>> _spawnPacketMap = new HashMap<>();

	// The map which stores entity UUIDs once they have been unloaded
	private final Map<UUID, LinkedList<DisguiseBase>> _entityDisguiseMap = new HashMap<>();

	// The map of which players should a disguise be shown to
	private final Map<DisguiseBase, Predicate<Player>> _disguisePlayerMap = new HashMap<>();

	private final HashSet<String> _blockedNames = new HashSet<>();

	private boolean _handlingPacket = false;

	private DisguiseManager()
	{
		super("Disguise Manager");

		require(PacketHandler.class)
				.addPacketHandler(this,
						PacketPlayOutNamedEntitySpawn.class,
						PacketPlayOutPlayerInfo.class,
						PacketPlayOutSpawnEntity.class,
						PacketPlayOutEntityMetadata.class,
						PacketPlayOutSpawnEntityLiving.class,
						PacketPlayOutUpdateAttributes.class,
						PacketPlayOutEntityEquipment.class,
						PacketPlayOutEntityVelocity.class,
						PacketPlayOutEntityLook.class,
						PacketPlayOutRelEntityMove.class,
						PacketPlayOutRelEntityMoveLook.class,
						PacketPlayOutEntityTeleport.class
				);

		createBedChunk();
	}

	@EventHandler
	public void cleanupLazyCallers(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOW)
			return;

		_spawnPacketMap.values().forEach(list -> list.removeIf(base -> base.getEntity() == null));
		_spawnPacketMap.values().removeIf(list -> list.size() == 0);
		_entityDisguiseMap.values().forEach(list -> list.removeIf(base -> base.getEntity() == null));
		_entityDisguiseMap.values().removeIf(list -> list.size() == 0);
		_disguisePlayerMap.keySet().removeIf(base -> base.getEntity() == null);
	}

	// We want to re-register entities that were reloaded by chunk loading
	@EventHandler
	public void onEntityAdd(ChunkAddEntityEvent event)
	{
		LinkedList<DisguiseBase> disguises = _entityDisguiseMap.remove(event.getEntity().getUniqueId());

		if (disguises != null)
		{
			disguises.forEach(disguise -> disguise.setEntity(event.getEntity()));
			_spawnPacketMap.put(event.getEntity().getEntityId(), disguises);
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void onEntityRemove(ChunkUnloadEvent event)
	{
		Set<Entity> careAbout = new HashSet<>();
		for (Entity entity : event.getChunk().getEntities())
		{
			if (_spawnPacketMap.containsKey(entity.getEntityId()))
			{
				careAbout.add(entity);
			}
		}

		// Run it a tick later so that if someone else happened to cancel the event, we won't fall for it
		runSync(() ->
		{
			for (Entity entity : careAbout)
			{
				if (!entity.isValid())
				{
					_entityDisguiseMap.put(entity.getUniqueId(), _spawnPacketMap.remove(entity.getEntityId()));
				}
			}
		});
	}

	private boolean containsSpawnDisguise(Player owner, DisguiseBase disguise)
	{
		return disguise != null && (_disguisePlayerMap.containsKey(disguise) && _disguisePlayerMap.get(disguise).test(owner));
	}

	public void disguise(DisguiseBase disguise, Runnable after)
	{
		disguise(disguise, after, player -> true);
	}

	public void disguise(DisguiseBase disguise, Predicate<Player> accept)
	{
		disguise(disguise, null, accept);
	}

	public void disguise(DisguiseBase disguise)
	{
		disguise(disguise, null, t -> true);
	}

	public void disguise(DisguiseBase disguise, Runnable after, Predicate<Player> accept)
	{
		UtilTasks.onMainThread(() ->
		{
			// First, add everything to handle future disguises
			DisguiseBase before = null;

			LinkedList<DisguiseBase> disguises = _spawnPacketMap.computeIfAbsent(disguise.getEntityId(), key -> new LinkedList<>());

			before = disguises.peekFirst();

			disguises.addFirst(disguise);

			_disguisePlayerMap.put(disguise, accept);

			// If the entity hasn't been spawned in yet, try to spawn it in and if that fails, remove everything

			boolean spawnedIn = false;

			if (!disguise.getEntity().valid)
			{
				disguise.attemptToSpawn();
				if (!disguise.getEntity().valid)
				{
					disguises.remove(disguise);
					_disguisePlayerMap.remove(disguise);
					return;
				}
				spawnedIn = true;
			}

			if (before != null)
			{
				before.onTransfer(disguise);
			}

			// todo figure out what this does
			if (disguise.getEntity() instanceof EntityPlayer && disguise instanceof DisguisePlayer)
			{
				if (!(disguise.getEntity()).getName().equalsIgnoreCase(((DisguisePlayer) disguise).getName()))
				{
					_blockedNames.add(disguise.getEntity().getName());
				}
			}

			if (!spawnedIn)
			{
				refreshTrackers(disguise.getEntity().getBukkitEntity());
			} else
			{
				disguise.markSpawnedIn();
			}

			disguise.onDisguise(true);

			if (after != null)
			{
				after.run();
			}
		}).run();
	}

	public boolean undisguise(DisguiseBase originalDisguise)
	{
		return undisguise(originalDisguise, UndisguiseReason.EXPLICIT);
	}

	public boolean undisguise(DisguiseBase originalDisguise, UndisguiseReason reason)
	{
		if (originalDisguise == null) return false;
		if (originalDisguise.getEntity() == null) return false;

		net.minecraft.server.v1_8_R3.Entity entity = originalDisguise.getEntity();

		LinkedList<DisguiseBase> activeDisguises = this._spawnPacketMap.get(originalDisguise.getEntityId());

		if (activeDisguises == null) return false;

		if (!activeDisguises.contains(originalDisguise)) return false;

		if (entity.getBukkitEntity() instanceof Player)
		{
			_blockedNames.remove(entity.getName());
		}

		Predicate<Player> test = _disguisePlayerMap.remove(originalDisguise);

		if (originalDisguise instanceof DisguisePlayer)
		{
			for (Player player : UtilServer.getPlayersCollection())
			{
				if (test.test(player))
				{
					EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
					entityPlayer.playerConnection.networkManager.handle(((DisguisePlayer) originalDisguise).getUndisguiseInfoPackets(true));
					if (reason != UndisguiseReason.QUIT)
					{
						Packet add = ((DisguisePlayer) originalDisguise).getUndisguiseInfoPackets(false);
						if (add != null)
						{
							entityPlayer.playerConnection.networkManager.handle(add);
						}

						// Cleanup team entries
						Scoreboard scoreboard = player.getScoreboard();

						if (scoreboard != null)
						{
							Team team = scoreboard.getTeam(HIDE_PLAYER_NAME_TEAM);

							if (team != null)
							{
								String name = ((DisguisePlayer) originalDisguise).getName();
								team.removeEntry(name);
							}
						}
					}
				}
			}
		}

		int index = activeDisguises.indexOf(originalDisguise);

		activeDisguises.remove(originalDisguise);

		if (activeDisguises.size() == 0)
			_spawnPacketMap.remove(originalDisguise.getEntityId());

		if (index == 0)
		{
			originalDisguise.onUndisguise(true);
			if (activeDisguises.size() > 0)
			{
				activeDisguises.getFirst().onReturn(originalDisguise);
			}

			if (reason != UndisguiseReason.QUIT)
			{
				refreshTrackers(entity.getBukkitEntity());
			}
		}
		else
		{
			originalDisguise.onUndisguise(false);
		}

		return true;
	}

	@Deprecated
	public void undisguise(Entity entity)
	{
		undisguise(getActiveDisguise(entity), UndisguiseReason.EXPLICIT);
	}

	@Deprecated
	/*
	 * @Deprecated Use getActiveDisguise instead
	 */
	public DisguiseBase getDisguise(LivingEntity entity)
	{
		return getActiveDisguise(entity);
	}

	public DisguiseBase getActiveDisguise(Entity entity)
	{
		return getActiveDisguise(entity.getEntityId());
	}

	public DisguiseBase getActiveDisguise(int entityId)
	{
		LinkedList<DisguiseBase> list = _spawnPacketMap.get(entityId);
		if (list != null && list.size() > 0)
		{
			return list.getFirst();
		}
		return null;
	}

	public LinkedList<DisguiseBase> getAllDisguises(Entity entity)
	{
		return _spawnPacketMap.get(entity.getEntityId());
	}

	private EntityTrackerEntry getEntityTracker(net.minecraft.server.v1_8_R3.Entity entity)
	{
		return ((WorldServer) entity.world).tracker.trackedEntities.get(entity.getId());
	}

	public void handle(PacketInfo packetInfo)
	{
		if (_handlingPacket)
			return;

		final Packet packet = packetInfo.getPacket();
		final Player owner = packetInfo.getPlayer();
		final PacketVerifier packetVerifier = packetInfo.getVerifier();
		final int protocol = ((CraftPlayer) owner).getHandle().getProtocol();

		if (packet instanceof PacketPlayOutPlayerInfo)
		{
			PacketPlayOutPlayerInfo packetPlayOutPlayerInfo = ((PacketPlayOutPlayerInfo) packet);
			packetPlayOutPlayerInfo.b.removeIf(next -> _blockedNames.contains(next.a().getName()));

			if (packetPlayOutPlayerInfo.b.size() == 0)
			{
				packetInfo.setCancelled(true);
			}
		}
		else if (packet instanceof PacketPlayOutSpawnEntity ||
				packet instanceof PacketPlayOutSpawnEntityLiving ||
				packet instanceof PacketPlayOutNamedEntitySpawn)
		{
			int entityId = -1;

			if (packet instanceof PacketPlayOutSpawnEntity)
			{
				entityId = ((PacketPlayOutSpawnEntity) packet).a;
			}
			else if (packet instanceof PacketPlayOutSpawnEntityLiving)
			{
				entityId = ((PacketPlayOutSpawnEntityLiving) packet).a;
			}
			else if (packet instanceof PacketPlayOutNamedEntitySpawn)
			{
				entityId = ((PacketPlayOutNamedEntitySpawn) packet).a;
			}

			DisguiseBase latestDisguise = getActiveDisguise(entityId);

			if (latestDisguise != null)
			{
				if (containsSpawnDisguise(owner, latestDisguise))
				{
					packetInfo.setCancelled(true);

					handleSpawnPackets(packetInfo.getVerifier(), latestDisguise, protocol);
				}
				else if (latestDisguise.isHideIfNotDisguised())
				{
					packetInfo.setCancelled(true);
				}
			}
		}
		else if (packet instanceof PacketPlayOutUpdateAttributes)
		{
			int entityId = ((PacketPlayOutUpdateAttributes) packet).a;

			DisguiseBase latestDisguise = getActiveDisguise(entityId);

			if (latestDisguise != null && containsSpawnDisguise(owner, latestDisguise))
			{
				// Crash clients with meta to a block id.
				if (latestDisguise instanceof DisguiseBlock)
					packetInfo.setCancelled(true);
			}
		}
		else if (packet instanceof PacketPlayOutEntityMetadata)
		{
			int entityId = ((PacketPlayOutEntityMetadata) packet).a;

			DisguiseBase latestDisguise = getActiveDisguise(entityId);

			if (latestDisguise != null && containsSpawnDisguise(owner, latestDisguise) && owner.getEntityId() != entityId)
			{
				packetInfo.setCancelled(true);
				handlePacket(latestDisguise.modifyMetaPacket(protocol, latestDisguise.getMetadataPacket()), packetVerifier);
			}
		}
		else if (packet instanceof PacketPlayOutEntityEquipment)
		{
			int entityId = ((PacketPlayOutEntityEquipment) packet).a;

			DisguiseBase latestDisguise = getActiveDisguise(entityId);

			if (latestDisguise != null && containsSpawnDisguise(owner, latestDisguise) && latestDisguise instanceof DisguiseInsentient)
			{
				if (!((DisguiseInsentient) latestDisguise).armorVisible() && ((PacketPlayOutEntityEquipment) packet).b != 0)
				{
					packetInfo.setCancelled(true);
				}
			}
		}
		else if (packet instanceof PacketPlayOutEntityVelocity)
		{
			PacketPlayOutEntityVelocity velocityPacket = (PacketPlayOutEntityVelocity) packet;

			DisguiseBase latestDisguise = getActiveDisguise(velocityPacket.a);

			// Squids will move using their current velocity every tick. So let's just not give them any velocities
			if (latestDisguise != null && latestDisguise instanceof DisguiseSquid && getActiveDisguise(owner) != latestDisguise)
			{
				packetInfo.setCancelled(true);
			}
		}
		else if (packet instanceof PacketPlayOutEntity)
		{
			PacketPlayOutEntity entityPacket = (PacketPlayOutEntity) packet;
			DisguiseBase latestDisguise = getActiveDisguise(entityPacket.a);

			if (latestDisguise != null && latestDisguise.isPitchLocked())
			{
				entityPacket.f = 0;
			}
		}
		else if (packet instanceof PacketPlayOutEntityTeleport)
		{
			PacketPlayOutEntityTeleport teleportPacket = (PacketPlayOutEntityTeleport) packet;
			DisguiseBase latestDisguise = getActiveDisguise(teleportPacket.a);

			if (latestDisguise != null && latestDisguise.isPitchLocked())
			{
				teleportPacket.f = 0;
			}
		}
	}

	private void handlePacket(Packet packet, PacketVerifier verifier)
	{
		if (packet == null) return;
		_handlingPacket = true;
		verifier.process(packet);
		_handlingPacket = false;
	}

	private void handleSpawnPackets(PacketVerifier packetVerifier, DisguiseBase disguise, int protocol)
	{
		if (disguise instanceof DisguisePlayer)
		{
			final DisguisePlayer pDisguise = (DisguisePlayer) disguise;

			Packet infoPacket = pDisguise.getDisguiseInfoPackets(false);
			if (infoPacket != null)
			{
				handlePacket(infoPacket, packetVerifier);
			}

			infoPacket = pDisguise.getDisguiseInfoPackets(true);
			if (infoPacket != null)
			{
				handlePacket(infoPacket, packetVerifier);
			}

			handlePacket(pDisguise.modifySpawnPacket(protocol, pDisguise.getSpawnPacket()), packetVerifier);

			for (Packet packet : pDisguise.getEquipmentPackets())
			{
				handlePacket(packet, packetVerifier);
			}

			handlePacket(pDisguise.modifyMetaPacket(protocol, pDisguise.getMetadataPacket()), packetVerifier);

			if (pDisguise.getSleepingDirection() != null)
			{
				for (Packet packet : getBedPackets(protocol, pDisguise))
				{
					handlePacket(packet, packetVerifier);
				}
			}

			if (!pDisguise.showInTabList())
			{
				Runnable r = () ->
				{
					PacketPlayOutPlayerInfo playerInfoPacketRemove = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER);
					PacketPlayOutPlayerInfo.PlayerInfoData dataRemove = playerInfoPacketRemove.new PlayerInfoData(pDisguise.getProfile(), 0, WorldSettings.EnumGamemode.SURVIVAL, null);
					playerInfoPacketRemove.b.add(dataRemove);
					handlePacket(playerInfoPacketRemove, packetVerifier);
				};
				if (pDisguise.getShowInTabListDelay() == 0)
				{
					r.run();
				}
				else
				{
					Bukkit.getScheduler().runTaskLater(UtilServer.getPlugin(), r, pDisguise.getShowInTabListDelay());
				}
			}
			else
			{
				if (!pDisguise.replaceOriginalName())
				{
					Runnable r = () ->
					{
						PacketPlayOutPlayerInfo playerInfoPacketRemove = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER);
						PacketPlayOutPlayerInfo.PlayerInfoData dataRemove = playerInfoPacketRemove.new PlayerInfoData(pDisguise.getProfile(), 0, WorldSettings.EnumGamemode.SURVIVAL, null);
						playerInfoPacketRemove.b.add(dataRemove);
						handlePacket(playerInfoPacketRemove, packetVerifier);
						PacketPlayOutPlayerInfo playerInfoPacketAdd = new PacketPlayOutPlayerInfo(PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER);
						PacketPlayOutPlayerInfo.PlayerInfoData dataAdd = playerInfoPacketRemove.new PlayerInfoData(pDisguise.getOriginalProfile(), 0, WorldSettings.EnumGamemode.SURVIVAL, null);
						playerInfoPacketAdd.b.add(dataAdd);
						handlePacket(playerInfoPacketAdd, packetVerifier);
					};
					if (pDisguise.replaceOriginalNameDelay() == 0)
					{
						r.run();
					}
					else
					{
						Bukkit.getScheduler().runTaskLater(UtilServer.getPlugin(), r, pDisguise.replaceOriginalNameDelay());
					}
				}
			}
		}
		else
		{
			handlePacket(disguise.modifySpawnPacket(protocol, disguise.getSpawnPacket()), packetVerifier);

			if (disguise instanceof DisguiseLiving)
			{
				ArrayList<Packet> packets = ((DisguiseLiving) disguise).getEquipmentPackets();

				for (Packet packet : packets)
				{
					handlePacket(packet, packetVerifier);
				}
			}
		}
	}

	public boolean isDisguised(LivingEntity entity)
	{
		return _spawnPacketMap.containsKey(entity.getEntityId());
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void PlayerQuit(PlayerQuitEvent event)
	{
		while (getActiveDisguise(event.getPlayer()) != null)
			undisguise(getActiveDisguise(event.getPlayer()), UndisguiseReason.QUIT);
	}

	private void refreshTrackers(Entity entity)
	{
		DisguiseBase activeDisguise = getActiveDisguise(entity);
		Predicate<Player> tester = _disguisePlayerMap.getOrDefault(activeDisguise, test -> true);

		final EntityTrackerEntry entityTracker = getEntityTracker(((CraftEntity) entity).getHandle());

		if (entityTracker != null)
		{
			if (entity.isValid())
			{
				for (Player player : PlayerSelector.selectPlayers(
						UtilLambda.and(
								PlayerSelector.inWorld(entity.getWorld()),
								OfflinePlayer::isOnline,
								tester
						)
					)
				)
				{
					EntityPlayer handle = ((CraftPlayer) player).getHandle();
					entityTracker.clear(handle);
					runSyncLater(() ->
					{
						/*
						 * This is like, the hackiest shit ever.
						 *
						 * Basically, we need to delay by an arbitrary amount of ticks (in this case, 5) because of the client.
						 *
						 * In the client, the renderer renders batches of 16x16x16, and entities are stored in ChunkSections.
						 * However, the data structure used is a HashMultimap, and the hashCode() method for Entity simply returns its entity id
						 *
						 * Now, due to an unfortunate coincidence, sending a PacketPlayOutEntityDestroy does not immediately remove an entity from the client.
						 * Instead, it queues it for removal on the next tick (why tf). This means that if we send a destroy and spawn packet one after the other,
						 * the process looks something like this
						 *
						 *  Received PacketPlayOutEntityDestroy
						 *  Queue removal of original entity
						 *  Received PacketPlayOutSpawnLivingEntity
						 *  Register entity in ChunkSection (based on entity id)
						 *  --- next tick ---
						 *  Removal of original entity from ChunkSection (which is now the new disguised entity
						 *
						 * So, what can we do?
						 *
						 * We could do this, where we delay an arbitrary amount of time and hope that the client processes the tick.
						 * However, a better long term solution would be to rewrite entity ids properly
						 */
						entityTracker.updatePlayer(((CraftPlayer) player).getHandle());
					}, 5L);
				}
			}
		}
	}

	public void updateDisguise(DisguiseBase disguise)
	{
		Predicate<Player> tester = _disguisePlayerMap.get(disguise);
		for (Player player : Bukkit.getOnlinePlayers())
		{
			if (tester.test(player))
			{
				EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
				if (disguise.getEntity() == nmsPlayer)
					continue;

				int protocol = nmsPlayer.getProtocol();
				UtilPlayer.sendPacket(player, disguise.modifyMetaPacket(protocol, disguise.getMetadataPacket()));
			}
		}
	}

	@EventHandler
	public void cleanDisguises(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOWER || _disguisePlayerMap.isEmpty())
			return;

		for (Iterator<DisguiseBase> disguiseIterator = _disguisePlayerMap.keySet().iterator(); disguiseIterator.hasNext(); )
		{
			DisguiseBase disguise = disguiseIterator.next();

			if (!(disguise.getEntity() instanceof EntityPlayer))
				continue;

			EntityPlayer disguisedPlayer = (EntityPlayer) disguise.getEntity();

			if (Bukkit.getPlayerExact(disguisedPlayer.getName()) == null || !disguisedPlayer.isAlive() || !disguisedPlayer.valid)
				disguiseIterator.remove();
		}
	}

	/**
	 * Handles hiding player disguises name tags if needed.
	 */
	@EventHandler
	public void hideNames(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		_disguisePlayerMap.forEach((disguise, showTest) ->
		{
			// Not a player, not interested.
			if (!(disguise instanceof DisguisePlayer))
			{
				return;
			}

			DisguisePlayer disguisePlayer = (DisguisePlayer) disguise;

			// Should show the name tag
			if (!disguisePlayer.hasHologram())
			{
				return;
			}

			// For all players
			Bukkit.getOnlinePlayers().forEach(player ->
			{
				// If the disguise is not to be shown
				if (!showTest.test(player))
				{
					return;
				}

				Scoreboard scoreboard = player.getScoreboard();

				// Null scoreboard
				if (scoreboard == null)
				{
					return;
				}

				// Team which all hidden players are placed in
				Team team = scoreboard.getTeam(HIDE_PLAYER_NAME_TEAM);

				// Team didn't exist
				if (team == null)
				{
					team = scoreboard.registerNewTeam(HIDE_PLAYER_NAME_TEAM);
					team.setNameTagVisibility(NameTagVisibility.NEVER);
					team.setPrefix(C.cDGray);
				}

				if (!team.hasEntry(disguisePlayer.getName()))
				{
					// Add the name of the disguised player into the team
					team.addEntry(disguisePlayer.getName());
				}
			});
		});
	}

	public enum UndisguiseReason
	{
		EXPLICIT,
		QUIT
	}

	/*
	 * =================================
	 * HERE BEGINS STUFF SOLELY TO DEAL
	 * WITH MAKING PLAYERS LOOK LIKE
	 * THEY'RE ON BEDS
	 *
	 * DON'T TOUCH UNLESS YOU KNOW
	 * EXACTLY WHAT YOU'RE DOING
	 * =================================
	 */

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void switchedWorld(PlayerChangedWorldEvent event)
	{
		chunkMove(event.getPlayer());
	}

	@EventHandler
	public void chunkJoin(PlayerJoinEvent event)
	{
		chunkMove(event.getPlayer());
	}

	private void chunkMove(Player player)
	{
		List<Packet> packets = new ArrayList<>();

		EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
		int protocol = nmsPlayer.getProtocol();

		PacketPlayOutMapChunk chunk = new PacketPlayOutMapChunk(protocol, _bedChunk, true, '\uffff');
		chunk.a = BED_POS_NORTH[0] >> 4;
		chunk.b = BED_POS_NORTH[2] >> 4;

		packets.add(chunk);

		_spawnPacketMap.entrySet().stream()
				.filter(entry -> entry.getValue().size() > 0 && entry.getValue().getFirst() instanceof DisguisePlayer && ((DisguisePlayer) entry.getValue().getFirst()).getSleepingDirection() != null)
				.filter(entry -> this.containsSpawnDisguise(player, entry.getValue().getFirst()))
				.forEach(entry ->
				{
					EntityTrackerEntry tracker = getEntityTracker(entry.getValue().getFirst().getEntity());

					if (tracker != null && tracker.trackedPlayers.contains(nmsPlayer))
					{
						packets.addAll(getBedPackets(protocol, (DisguisePlayer) entry.getValue().getFirst()));
					}
				});

		for (Packet packet : packets)
		{
			UtilPlayer.sendPacket(player, packet);
		}
	}

	/*
	 * Create the packets needed to make this DisguisePlayer appear as though laying on a bed
	 *
	 * fixme can we make this better at all?!?!?!
	 */
	private List<Packet> getBedPackets(int protocol, DisguisePlayer playerDisguise)
	{
		int[] coords = getCoordsFor(playerDisguise);

		List<Packet> packets = new ArrayList<>();
		PacketPlayOutBed bedPacket = new PacketPlayOutBed();
		bedPacket.a = playerDisguise.getEntityId();
		bedPacket.b = new BlockPosition(coords[0], coords[1], coords[2]);
		packets.add(bedPacket);

		int partitions = 3;
		double posX = coords[0], posY = coords[1], posZ = coords[2];
		double targetX = playerDisguise.getEntity().locX, targetY = playerDisguise.getEntity().locY, targetZ = playerDisguise.getEntity().locZ;

		while (partitions > 1)
		{
			double d0 = posX + (targetX - posX) / (double) partitions;
			double d1 = posY + (targetY - posY) / (double) partitions;
			double d2 = posZ + (targetZ - posZ) / (double) partitions;


			PacketPlayOutMapChunk chunk = new PacketPlayOutMapChunk(protocol, _bedChunk, true, '\uffff');
			chunk.a = (int) Math.floor(d0) >> 4;
			chunk.b = (int) Math.floor(d2) >> 4;
			packets.add(chunk);
			partitions--;

			posX = d0;
			posY = d1;
			posZ = d2;
		}

		PacketPlayOutEntityTeleport teleportPacket = new PacketPlayOutEntityTeleport(playerDisguise.getEntity());
		packets.add(teleportPacket);

		return packets;
	}


	private static final int[] BED_POS_NORTH = {-29999999, 0, -29999999};
	private static final int[] BED_POS_SOUTH = {-29999999, 0, -29999998};
	private static final int[] BED_POS_EAST = {-29999999, 0, -29999997};
	private static final int[] BED_POS_WEST = {-29999999, 0, -29999996};

	private Chunk _bedChunk;

	private void createBedChunk()
	{
		this._bedChunk = new Chunk(MinecraftServer.getServer().getWorld(), 0, 0);
		this._bedChunk.a(new BlockPosition(BED_POS_NORTH[0], BED_POS_NORTH[1], BED_POS_NORTH[2]), Blocks.BED.getBlockData().set(BlockBed.FACING, EnumDirection.NORTH));
		this._bedChunk.a(new BlockPosition(BED_POS_SOUTH[0], BED_POS_SOUTH[1], BED_POS_SOUTH[2]), Blocks.BED.getBlockData().set(BlockBed.FACING, EnumDirection.SOUTH));
		this._bedChunk.a(new BlockPosition(BED_POS_EAST[0], BED_POS_EAST[1], BED_POS_EAST[2]), Blocks.BED.getBlockData().set(BlockBed.FACING, EnumDirection.EAST));
		this._bedChunk.a(new BlockPosition(BED_POS_WEST[0], BED_POS_WEST[1], BED_POS_WEST[2]), Blocks.BED.getBlockData().set(BlockBed.FACING, EnumDirection.WEST));
	}

	private int[] getCoordsFor(DisguisePlayer player)
	{
		BlockFace facing = player.getSleepingDirection();
		switch (facing)
		{
			case NORTH:
				return BED_POS_SOUTH;
			case SOUTH:
				return BED_POS_NORTH;
			case EAST:
				return BED_POS_WEST;
			case WEST:
				return BED_POS_EAST;
		}
		throw new IllegalArgumentException("Unsupported blockface " + facing);
	}
}
