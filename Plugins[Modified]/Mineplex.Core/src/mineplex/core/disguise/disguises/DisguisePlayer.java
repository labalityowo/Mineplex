package mineplex.core.disguise.disguises;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

import net.minecraft.server.v1_8_R3.AttributeInstance;
import net.minecraft.server.v1_8_R3.AttributeMapServer;
import net.minecraft.server.v1_8_R3.EntityHuman;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.IInventory;
import net.minecraft.server.v1_8_R3.ITileEntityContainer;
import net.minecraft.server.v1_8_R3.MathHelper;
import net.minecraft.server.v1_8_R3.MobEffect;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutAbilities;
import net.minecraft.server.v1_8_R3.PacketPlayOutAnimation;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityEffect;
import net.minecraft.server.v1_8_R3.PacketPlayOutExperience;
import net.minecraft.server.v1_8_R3.PacketPlayOutHeldItemSlot;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_8_R3.PacketPlayOutOpenWindow;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo.EnumPlayerInfoAction;
import net.minecraft.server.v1_8_R3.PacketPlayOutPosition;
import net.minecraft.server.v1_8_R3.PacketPlayOutRespawn;
import net.minecraft.server.v1_8_R3.PacketPlayOutUpdateAttributes;
import net.minecraft.server.v1_8_R3.PacketPlayOutUpdateHealth;
import net.minecraft.server.v1_8_R3.WorldSettings;

import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftInventory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import com.mojang.authlib.GameProfile;

import mineplex.core.Managers;
import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.UtilMath;
import mineplex.core.disguise.playerdisguise.PlayerDisguiseManager;
import mineplex.core.hologram.Hologram;
import mineplex.core.hologram.Hologram.HologramTarget;
import mineplex.core.hologram.HologramManager;
import mineplex.core.thread.ThreadPool;
import mineplex.core.utils.UtilGameProfile;

public class DisguisePlayer extends DisguiseHuman
{
	private String _requestedUsername;

	// If _requestedSkinData is not null, that will be used
	private SkinData _requestedSkinData;
	private String _requestedSkin;

	private GameProfile _originalProfile;
	private GameProfile _profile;

	private BlockFace _sleeping;

	private boolean _sendSkinToSelf = true;
	private boolean _showInTabList = false;
	private boolean _replaceOriginalName = true;
	private int _showInTabListDelay = 30;
	private int _replaceOriginalNameDelay;
	private Hologram _hologram;

	private DisguisePlayer(Entity entity)
	{
		super(EntityType.PLAYER, entity);
		_originalProfile = entity instanceof Player ? UtilGameProfile.getGameProfile((Player) entity) : null;
	}

	/**
	 * Using this constructor, this DisguisePlayer will be given the same GameProfile as provided
	 */
	public DisguisePlayer(Entity entity, GameProfile gameProfile)
	{
		this(entity);

		_profile = UtilGameProfile.clone(gameProfile);
	}

	/**
	 * @param username The username to disguise this player as, AND the username of the player whose skin will be used
	 */
	public DisguisePlayer(Entity entity, String username)
	{
		this(entity, username, username);
	}

	/**
	 * @param username The username to disguise this entity as
	 * @param skin     The username of the player whose skin will be used
	 */
	public DisguisePlayer(Entity entity, String username, String skin)
	{
		this(entity);

		_requestedUsername = username;
		_requestedSkin = skin;
	}

	public DisguisePlayer(Entity entity, String username, SkinData skinData)
	{
		this(entity);

		_requestedUsername = username;
		_requestedSkinData = skinData;
	}

	/**
	 * If this DisguisePlayer has been initialized with a requested username and requested skin, it must be initialized
	 *
	 * @param onComplete The Runnable which will be run once initialized. Can be null. It will be run on a separate thread if initialization took place, and the current thread if not
	 * @returns A Future which, upon completion, implies the task is done
	 */
	public Future<Object> initialize(Runnable onComplete)
	{
		if (_profile != null && _profile.isComplete())
		{
			onComplete.run();
			return CompletableFuture.completedFuture(null);
		}

		return ThreadPool.ASYNC.submit(() ->
		{
			try
			{
				GameProfile profileOfUsername = UtilGameProfile.getProfileByName(_requestedUsername, true, null).get();

				if (_requestedSkinData == null)
				{
					GameProfile profileOfSkin = UtilGameProfile.getProfileByName(_requestedSkin, true, null).get();
					_requestedSkinData = SkinData.constructFromGameProfile(profileOfSkin, true, true);
				}

				_profile = new GameProfile(profileOfUsername.getId(), profileOfUsername.getName());
				_profile.getProperties().put("textures", _requestedSkinData.getProperty());
			}
			catch (Exception e)
			{
				// fixme handle
				e.printStackTrace();
			}
			finally
			{
				onComplete.run();
			}
		}, null);
	}

	public GameProfile getProfile()
	{
		return _profile;
	}

	public void setSendSkinDataToSelf(boolean sendToSelf)
	{
		_sendSkinToSelf = sendToSelf;
	}

	public boolean getSendSkinDataToSelf()
	{
		return _sendSkinToSelf;
	}

	public void showInTabList(boolean show, int delay)
	{
		_showInTabList = show;
		_showInTabListDelay = delay;
	}

	public BlockFace getSleepingDirection()
	{
		return _sleeping;
	}

	/**
	 * Don't use this if the disguise is already on as it will not work the way you want it to. Contact samczsun if you need
	 * that added.
	 * <p>
	 * BlockFace.NORTH = feet pointing north
	 * BlockFace.SOUTH = feet pointing south
	 * etc etc
	 */
	public void setSleeping(BlockFace sleeping)
	{
		_sleeping = sleeping;
	}

	public PacketPlayOutPlayerInfo getDisguiseInfoPackets(boolean add)
	{
		if (!add && _originalProfile == null)
			return null;

		PacketPlayOutPlayerInfo newDisguiseInfo = new PacketPlayOutPlayerInfo();
		newDisguiseInfo.a = add ? EnumPlayerInfoAction.ADD_PLAYER : EnumPlayerInfoAction.REMOVE_PLAYER;

		PacketPlayOutPlayerInfo.PlayerInfoData info = newDisguiseInfo.new PlayerInfoData(add ? _profile : _originalProfile, UtilMath.r(120),
				getAppropriateGamemode(), null);

		newDisguiseInfo.b.add(info);

		return newDisguiseInfo;
	}

	public Packet getUndisguiseInfoPackets(boolean remove)
	{
		if (!remove && _originalProfile == null)
			return null;

		PacketPlayOutPlayerInfo newDisguiseInfo = new PacketPlayOutPlayerInfo();
		newDisguiseInfo.a = remove ? EnumPlayerInfoAction.REMOVE_PLAYER : EnumPlayerInfoAction.ADD_PLAYER;

		PacketPlayOutPlayerInfo.PlayerInfoData info = newDisguiseInfo.new PlayerInfoData(remove ? _profile : _originalProfile, UtilMath.r(120),
				getAppropriateGamemode(), null);

		newDisguiseInfo.b.add(info);

		return newDisguiseInfo;
	}

	@Override
	public void UpdateDataWatcher()
	{
		super.UpdateDataWatcher();

		byte b0 = DataWatcher.getByte(0);

		if (getEntity().isSneaking())
		{
			DataWatcher.watch(0, Byte.valueOf((byte) (b0 | 1 << 1)), EntityHuman.META_ENTITYDATA, (byte) (b0 | 1 << 1));
		}
		else
		{
			DataWatcher.watch(0, Byte.valueOf((byte) (b0 & ~(1 << 1))), EntityHuman.META_ENTITYDATA, (byte) (b0 & ~(1 << 1)));
		}

		if (getEntity() instanceof EntityPlayer)
		{
			EntityPlayer entityPlayer = (EntityPlayer) getEntity();

			DataWatcher.watch(10, entityPlayer.getDataWatcher().getByte(10), EntityPlayer.META_SKIN, entityPlayer.getDataWatcher().getByte(10));
			DataWatcher.watch(16, (byte) 0, EntityPlayer.META_CAPE, (byte) 1);
		}
	}

	@Override
	public PacketPlayOutNamedEntitySpawn getSpawnPacket()
	{
		PacketPlayOutNamedEntitySpawn packet = new PacketPlayOutNamedEntitySpawn();
		packet.a = getEntity().getId();
		packet.b = _profile.getId();
		packet.c = MathHelper.floor(getEntity().locX * 32.0D);
		packet.d = MathHelper.floor(getEntity().locY * 32.0D);
		packet.e = MathHelper.floor(getEntity().locZ * 32.0D);
		packet.f = (byte) ((int) ((getEntity().isFakeHead() ? getEntity().fakeYaw : getEntity().yaw) * 256.0F / 360.0F));
		packet.g = (byte) ((int) ((getEntity().isFakeHead() ? getEntity().fakePitch : getEntity().pitch) * 256.0F / 360.0F));
		packet.i = DataWatcher;

		return packet;
	}

	public String getName()
	{
		return _profile.getName();
	}

	public void sendHit()
	{
		PacketPlayOutAnimation packet = new PacketPlayOutAnimation();
		packet.a = getEntityId();
		packet.b = 0;

		sendPacket(packet);
	}

	@Override
	public void onDisguise(boolean isActive)
	{
		if (this.getEntity() instanceof EntityPlayer)
		{
			if (_sendSkinToSelf)
			{
				EntityPlayer entityPlayer = ((EntityPlayer) this.getEntity());

				// First construct the packet which will remove the previous disguise from the tab list
				PacketPlayOutPlayerInfo playerInfoPacketRemove = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER);
				PacketPlayOutPlayerInfo.PlayerInfoData dataRemove = playerInfoPacketRemove.new PlayerInfoData(_originalProfile, entityPlayer.ping, getAppropriateGamemode(), null);
				playerInfoPacketRemove.b.add(dataRemove);

				// This packet will add the new disguised name into the tab list
				// We have to create a new profile with the original ID because when we respawn the caller, their UUID stays the same
				PacketPlayOutPlayerInfo playerInfoPacket = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER);
				PacketPlayOutPlayerInfo.PlayerInfoData data = playerInfoPacket.new PlayerInfoData(getSelfProfile(), entityPlayer.ping, getAppropriateGamemode(), null);
				playerInfoPacket.b.add(data);

				// This packet does the magic. It forces a new player to be created client-side which causes the skin to reload
				PacketPlayOutRespawn respawnPacket = new PacketPlayOutRespawn(entityPlayer.world.getWorld().getEnvironment().getId(), entityPlayer.getWorld().getDifficulty(), entityPlayer.getWorld().worldData.getType(), getAppropriateGamemode());

				entityPlayer.playerConnection.networkManager.handle(playerInfoPacketRemove);
				entityPlayer.playerConnection.networkManager.handle(playerInfoPacket);
				entityPlayer.playerConnection.networkManager.handle(respawnPacket);

				update(entityPlayer);
			}
		}

		if (hasHologram())
		{
			_hologram.start();
		}
	}

	@Override
	public void onUndisguise(boolean wasActive)
	{
		if (this.getEntity() instanceof EntityPlayer)
		{
			if (_sendSkinToSelf)
			{
				EntityPlayer entityPlayer = ((EntityPlayer) this.getEntity());

				// First construct the packet which will remove the previous disguise from the tab list
				PacketPlayOutPlayerInfo playerInfoPacketRemove = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER);
				PacketPlayOutPlayerInfo.PlayerInfoData dataRemove = playerInfoPacketRemove.new PlayerInfoData(getSelfProfile(), entityPlayer.ping, getAppropriateGamemode(), null);
				playerInfoPacketRemove.b.add(dataRemove);

				// This packet will add the new disguised name into the tab list
				// We have to create a new profile with the original ID because when we respawn the caller, their UUID stays the same

				PacketPlayOutPlayerInfo playerInfoPacket = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER);
				PacketPlayOutPlayerInfo.PlayerInfoData data = playerInfoPacket.new PlayerInfoData(_originalProfile, entityPlayer.ping, entityPlayer.playerInteractManager.getGameMode(), null);
				playerInfoPacket.b.add(data);

				// This packet does the magic. It forces a new player to be created client-side which causes the skin to reload
				PacketPlayOutRespawn respawnPacket = new PacketPlayOutRespawn(entityPlayer.world.getWorld().getEnvironment().getId(), entityPlayer.getWorld().getDifficulty(), entityPlayer.getWorld().worldData.getType(), getAppropriateGamemode());

				entityPlayer.playerConnection.networkManager.handle(playerInfoPacketRemove);
				entityPlayer.playerConnection.networkManager.handle(playerInfoPacket);
				entityPlayer.playerConnection.networkManager.handle(respawnPacket);

				update(entityPlayer);
			}
		}

		if (hasHologram())
		{
			_hologram.stop();
		}
	}

	@Override
	public void onTransfer(DisguiseBase disguise)
	{
		if (disguise instanceof DisguisePlayer)
		{
			if (((DisguisePlayer) disguise).getSendSkinDataToSelf() && _sendSkinToSelf && this.getEntity() instanceof EntityPlayer)
			{
				EntityPlayer entityPlayer = ((EntityPlayer) this.getEntity());

				PacketPlayOutPlayerInfo playerInfoRemove = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER);
				PacketPlayOutPlayerInfo.PlayerInfoData data = playerInfoRemove.new PlayerInfoData(getSelfProfile(), entityPlayer.ping, getAppropriateGamemode(), null);
				playerInfoRemove.b.add(data);

				entityPlayer.playerConnection.networkManager.handle(playerInfoRemove);
			}
		}
	}

	@EventHandler
	public void onReturn(DisguiseBase disguise)
	{
		if (disguise instanceof DisguisePlayer)
		{
			if (((DisguisePlayer) disguise).getSendSkinDataToSelf() && _sendSkinToSelf && this.getEntity() instanceof EntityPlayer)
			{
				EntityPlayer entityPlayer = ((EntityPlayer) this.getEntity());

				PacketPlayOutPlayerInfo playerInfoPacket = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.REMOVE_PLAYER);
				PacketPlayOutPlayerInfo.PlayerInfoData data = playerInfoPacket.new PlayerInfoData(((DisguisePlayer) disguise)._originalProfile, entityPlayer.ping, entityPlayer.playerInteractManager.getGameMode(), null);
				playerInfoPacket.b.add(data);

				entityPlayer.playerConnection.networkManager.handle(playerInfoPacket);

				PacketPlayOutPlayerInfo playerInfoAdd = new PacketPlayOutPlayerInfo(EnumPlayerInfoAction.ADD_PLAYER);
				PacketPlayOutPlayerInfo.PlayerInfoData dataRemove = playerInfoAdd.new PlayerInfoData(getSelfProfile(), entityPlayer.ping, entityPlayer.playerInteractManager.getGameMode(), null);
				playerInfoAdd.b.add(dataRemove);

				entityPlayer.playerConnection.networkManager.handle(playerInfoAdd);
			}
		}
	}

	public GameProfile getOriginalProfile()
	{
		return UtilGameProfile.clone(_originalProfile);
	}

	private void update(EntityPlayer entityPlayer)
	{
		// And then we teleport them back to where they were
		PacketPlayOutPosition positionPacket = new PacketPlayOutPosition(entityPlayer.locX, entityPlayer.locY, entityPlayer.locZ, entityPlayer.yaw, entityPlayer.pitch, new HashSet<>());

		// Allow them to fly again
		PacketPlayOutAbilities packetPlayOutAbilities = new PacketPlayOutAbilities(entityPlayer.abilities);

		// Give them their exp again
		PacketPlayOutExperience exp = new PacketPlayOutExperience(entityPlayer.exp, entityPlayer.expTotal, entityPlayer.expLevel);

		entityPlayer.playerConnection.networkManager.handle(positionPacket);
		entityPlayer.playerConnection.networkManager.handle(packetPlayOutAbilities);
		entityPlayer.playerConnection.networkManager.handle(exp);
		if (entityPlayer.activeContainer != entityPlayer.defaultContainer)
		{
			IInventory inv = ((CraftInventory) entityPlayer.activeContainer.getBukkitView().getTopInventory()).getInventory();
			String name;
			if (inv instanceof ITileEntityContainer)
			{
				name = ((ITileEntityContainer) inv).getContainerName();
			}
			else
			{
				name = "minecraft:chest";
			}

			entityPlayer.playerConnection.sendPacket(new PacketPlayOutOpenWindow(entityPlayer.activeContainer.windowId, name, inv.getScoreboardDisplayName(), inv.getSize()));
		}
		entityPlayer.updateInventory(entityPlayer.activeContainer);
		entityPlayer.playerConnection.networkManager.handle(new PacketPlayOutHeldItemSlot(entityPlayer.inventory.itemInHandIndex));

		AttributeMapServer attributemapserver = (AttributeMapServer) entityPlayer.getAttributeMap();
		Set<AttributeInstance> set = attributemapserver.getAttributes();

		entityPlayer.getBukkitEntity().injectScaledMaxHealth(set, true);

		entityPlayer.playerConnection.sendPacket(new PacketPlayOutUpdateHealth(entityPlayer.getBukkitEntity().getScaledHealth(), entityPlayer.getFoodData().getFoodLevel(), entityPlayer.getFoodData().getSaturationLevel()));
		entityPlayer.playerConnection.sendPacket(new PacketPlayOutUpdateAttributes(entityPlayer.getId(), set));

		for (MobEffect mobEffect : entityPlayer.getEffects())
		{
			if (entityPlayer.playerConnection.networkManager.getVersion() > 47 || mobEffect.getEffectId() != 25)
				entityPlayer.playerConnection.sendPacket(new PacketPlayOutEntityEffect(entityPlayer.getId(), mobEffect));
		}
	}

	public boolean showInTabList()
	{
		return _showInTabList;
	}

	public int getShowInTabListDelay()
	{
		return _showInTabListDelay;
	}

	private WorldSettings.EnumGamemode getAppropriateGamemode()
	{
		if (getEntity() instanceof EntityPlayer)
		{
			return ((EntityPlayer) getEntity()).playerInteractManager.getGameMode();
		}
		return WorldSettings.EnumGamemode.SURVIVAL;
	}

	private GameProfile getSelfProfile()
	{
		GameProfile selfProfile = new GameProfile(getOriginalUUID(), _profile.getName());
		selfProfile.getProperties().putAll(_profile.getProperties());
		return selfProfile;
	}

	public boolean replaceOriginalName()
	{
		return _replaceOriginalName;
	}

	public int replaceOriginalNameDelay()
	{
		return _replaceOriginalNameDelay;
	}

	public void setReplaceOriginalName(boolean b, int delay)
	{
		_replaceOriginalName = b;
		_replaceOriginalNameDelay = delay;
	}

	public Hologram getHologram()
	{
		if (_hologram == null)
		{
			_hologram = new Hologram(Managers.require(HologramManager.class), getEntity().getBukkitEntity().getLocation().add(0, 1.75, 0), true)
					.setRemoveOnEntityDeath();
		}

		return _hologram;
	}

	public boolean hasHologram()
	{
		return _hologram != null;
	}

	private UUID getOriginalUUID()
	{
		if (_originalProfile.getProperties().containsKey(PlayerDisguiseManager.ORIGINAL_UUID_KEY))
		{
			try
			{
				return UUID.fromString(_originalProfile.getProperties().get(PlayerDisguiseManager.ORIGINAL_UUID_KEY).iterator().next().getValue());
			}
			catch (IllegalArgumentException ignored) {}
		}
		return _originalProfile.getId();
	}
}