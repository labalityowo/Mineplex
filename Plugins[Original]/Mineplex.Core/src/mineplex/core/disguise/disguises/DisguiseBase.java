package mineplex.core.disguise.disguises;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import net.minecraft.server.v1_8_R3.*;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent;

import mineplex.core.common.DummyEntity;
import mineplex.core.common.util.UtilPlayer;

public abstract class DisguiseBase
{
	private WeakReference<Entity> _entity = new WeakReference<>(null);

	protected DataWatcher DataWatcher;

	private DisguiseBase _soundDisguise;

	private EntityType _disguiseType;

	/**
	 * Whether the disguised entity should be entirely hidden from a player if that player does not receive the disguise
	 */
	private boolean _hideIfNotDisguised = false;
	/**
	 * Whether the entity should have it's pitch locked at 0, prevents ESP being used.
	 */
	private boolean _lockPitch;

	protected boolean _spawnedIn = false;

	public DisguiseBase(EntityType entityType, org.bukkit.entity.Entity entity)
	{
		if (entity == null)
		{
			throw new NullPointerException("Entity cannot be null (did you mean to pass in an unspawned entity?)");
		}

		this._disguiseType = entityType;

		setEntity(entity);

		DataWatcher = new DataWatcher(new DummyEntity(null));

		DataWatcher.a(0, (byte) 0, net.minecraft.server.v1_8_R3.Entity.META_ENTITYDATA, (byte) 0);
		DataWatcher.a(1, (short) 300, net.minecraft.server.v1_8_R3.Entity.META_AIR, 300);

		_soundDisguise = this;
	}

	public void attemptToSpawn()
	{
		this.getEntity().world.addEntity(this.getEntity(), CreatureSpawnEvent.SpawnReason.CUSTOM);
	}

	public void setEntity(org.bukkit.entity.Entity entity)
	{
		setEntity(((CraftEntity) entity).getHandle());
	}

	public void UpdateDataWatcher()
	{
		DataWatcher.watch(0, getEntity().getDataWatcher().getByte(0), net.minecraft.server.v1_8_R3.Entity.META_ENTITYDATA, getEntity().getDataWatcher().getByte(0));
		DataWatcher.watch(1, getEntity().getDataWatcher().getShort(1), net.minecraft.server.v1_8_R3.Entity.META_AIR, (int) getEntity().getDataWatcher().getShort(1));
	}

	protected void sendToWatchers(Predicate<Integer> protocolPredicate, Supplier<Packet> supplier)
	{
		if (getEntity() == null || !getEntity().getBukkitEntity().isValid() || !(getEntity().world instanceof WorldServer))
			return;

		IntHashMap<EntityTrackerEntry> tracker = ((WorldServer) getEntity().world).tracker.trackedEntities;

		if (tracker.get(getEntity().getId()) == null)
			return;

		Packet packet = supplier.get();
		if (packet == null)
			return;

		for (EntityPlayer player : tracker.get(getEntity().getId()).trackedPlayers)
		{
			int protocol = player.getProtocol();
			if (!protocolPredicate.test(protocol))
				continue;

			if (packet instanceof PacketPlayOutEntityMetadata)
			{
				player.playerConnection.sendPacket(modifyMetaPacket(protocol, packet));
			} else if (packet instanceof PacketPlayOutSpawnEntityLiving)
			{
				player.playerConnection.sendPacket(modifySpawnPacket(protocol, packet));
			} else
			{
				player.playerConnection.sendPacket(packet);
			}
		}
	}

	protected void sendToWatchers(Supplier<Packet> supplier)
	{
		sendToWatchers(x -> true, supplier);
	}

	public abstract Packet getSpawnPacket();

	public Packet modifySpawnPacket(int protocol, Packet packet)
	{
		return packet;
	}

	public Packet getMetadataPacket()
	{
		UpdateDataWatcher();
		return new PacketPlayOutEntityMetadata(getEntity().getId(), DataWatcher, true);
	}

	public void resendMetadata()
	{
		sendToWatchers(this::getMetadataPacket);
	}

	public Packet modifyMetaPacket(int protocol, Packet packet)
	{
		return packet;
	}

	public void setSoundDisguise(DisguiseBase soundDisguise)
	{
		_soundDisguise = soundDisguise;

		if (_soundDisguise == null)
			_soundDisguise = this;
	}

	public void playHurtSound()
	{
		getEntity().world.makeSound(getEntity(), _soundDisguise.getHurtSound(), _soundDisguise.getVolume(), _soundDisguise.getPitch());
	}

	public void setLockPitch(boolean lockPitch)
	{
		_lockPitch = lockPitch;
	}

	public boolean isPitchLocked()
	{
		return _lockPitch;
	}

	public Entity getEntity()
	{
		return _entity.get();
	}

	public int getEntityId()
	{
		return getEntity().getId();
	}

	protected abstract String getHurtSound();

	protected abstract float getVolume();

	protected abstract float getPitch();

	public List<Player> getTrackedPlayers()
	{
		List<Player> players = new ArrayList<>();
		IntHashMap<EntityTrackerEntry> tracker = ((WorldServer) getEntity().world).tracker.trackedEntities;
		if (tracker.get(getEntity().getId()) == null)
		{
			System.out.println("Tracker did not contain " + getEntity().getId() + " " + getEntity().getCustomName() + " " + getEntity().dead + " " + getEntity().locX + " " + getEntity().locY + " " + getEntity().locZ);
			return Collections.emptyList();
		}
		for (EntityPlayer ep : tracker.get(getEntity().getId()).trackedPlayers)
		{
			players.add(ep.getBukkitEntity());
		}
		return players;
	}

	public void sendPacket(Packet... packet)
	{
		List<Player> trackedPlayers = getTrackedPlayers();
		for (Packet p : packet)
		{
			if (p instanceof PacketPlayOutPlayerInfo)
			{
				MinecraftServer.getServer().getPlayerList().sendAll(p);
			}
			else
			{
				for (Player player : trackedPlayers)
				{
					UtilPlayer.sendPacket(player, p);
				}
			}
		}
	}

	public void onDisguise(boolean isActive)
	{

	}

	public void onUndisguise(boolean wasActive)
	{

	}

	public void onTransfer(DisguiseBase other)
	{

	}

	public void onReturn(DisguiseBase other)
	{

	}

	public void markSpawnedIn()
	{
		_spawnedIn = true;
	}

	public void setHideIfNotDisguised(boolean hideIfNotDisguised)
	{
		this._hideIfNotDisguised = hideIfNotDisguised;
	}

	public boolean isHideIfNotDisguised()
	{
		return this._hideIfNotDisguised;
	}

	public EntityType getDisguiseType()
	{
		return this._disguiseType;
	}

	public void setEntity(Entity entity)
	{
		_entity.clear();
		_entity = new WeakReference<>(entity);
	}
}
