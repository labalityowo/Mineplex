package mineplex.core.disguise.playerdisguise;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import net.minecraft.server.v1_8_R3.MinecraftServer;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedEntitySpawn;
import net.minecraft.server.v1_8_R3.PacketPlayOutPlayerInfo;
import net.minecraft.server.v1_8_R3.PlayerList;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.google.common.collect.ImmutableSet;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.properties.Property;

import mineplex.core.MiniPlugin;
import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.account.CoreClient;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.skin.SkinData;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.disguise.DisguiseManager;
import mineplex.core.disguise.disguises.DisguisePlayer;
import mineplex.core.disguise.playerdisguise.events.PlayerDisguisedEvent;
import mineplex.core.disguise.playerdisguise.events.PlayerPreDisguiseEvent;
import mineplex.core.disguise.playerdisguise.events.PlayerPreUndisguiseEvent;
import mineplex.core.disguise.playerdisguise.events.PlayerUndisguisedEvent;
import mineplex.core.event.JoinMessageBroadcastEvent;
import mineplex.core.friend.FriendManager;
import mineplex.core.packethandler.IPacketHandler;
import mineplex.core.packethandler.PacketHandler;
import mineplex.core.packethandler.PacketInfo;
import mineplex.core.preferences.PreferencesManager;
import mineplex.core.punish.Punish;
import mineplex.core.punish.PunishClient;
import mineplex.core.scoreboard.ScoreboardManager;
import mineplex.core.utils.UtilGameProfile;
import mineplex.serverdata.Region;
import mineplex.serverdata.data.PlayerStatus;
import mineplex.serverdata.redis.RedisDataRepository;
import mineplex.serverdata.servers.ServerManager;

@ReflectivelyCreateMiniPlugin
public class PlayerDisguiseManager extends MiniPlugin implements IPacketHandler
{
	public enum Perm implements Permission
	{
		USE_DISGUISE,
		BLOCKED_DISGUISE,
		SHORT_DISGUISE,
	}

	private static final Set<String> MOJANG;
	private static final Set<String> ILLEGAL_USERNAMES;
	private static final Set<String> ILLEGAL_CAPES;

	private static final Set<String> VERY_SPECIAL_PEOPLE;

	static
	{
		MOJANG = ImmutableSet.copyOf(Arrays.asList("____fox____", "_tommo_", "aeplh", "amir343", "angryem", "ashrafi",
				"binni", "blurpi", "bopogamel", "c418", "carlmanneh", "carnalizer", "darngeek", "dinnerbone", "eldrone",
				"elevenen", "engst", "excitedze", "frukthamster", "geuder", "grumm", "hampus", "helloiammarsh", "hey",
				"hoodad", "jeb_", "jonkagstrom", "kappe", "klumpig", "krisjelbring", "ladyagnes", "lisa", "mahuldur",
				"mansolson", "marc", "marc_irl", "masseffect", "midnightenforcer", "minecraftchick", "modhelius",
				"mojangjonas", "mojangsta", "mollstam", "neonmaster", "notch", "olle", "olofcarlson", "phreakholm",
				"poipoichen", "pretto", "profmobius", "razzleberryfox", "searge", "searge_dp", "shoghicp", "slicedlime",
				"sockerpappan", "themogminer", "vaktis", "vubui", "xlson", "xsson", "yoloswag4lyfe", "zeeraw")
		);

		ILLEGAL_USERNAMES = ImmutableSet.copyOf(Arrays.asList("hypixel", "chiss", "dctr", "blondebug", "dooskee",
				"tomcallister", "jessiemarcia", "spu_", "sp614x", "deadmau5", "gwen", "mineplex", "samczsun", "sethbling",
				"xisuma", "cubehamster", "natet_bird", "qwertyuiopthepie", "hitler", "adolfhitler"
		));

		VERY_SPECIAL_PEOPLE = ImmutableSet.copyOf(Arrays.asList(
				"5399b615-3440-4c66-939d-ab1375952ac3", // Drullkus (Prismarine Cape)
				"7f0eda55-7034-4dc8-886d-d94321cdedcf", // MrMessiah (Personal Cape)
				"d90b68bc-8172-4329-a047-f1186dcd4336", // akronman1 (Millionth Customer)
				"144ad5f0-e879-4141-a489-8ed5d496cab9", // JulianClark (Personal Cape)
				"1c063715-395b-4db9-bc2a-d5dfd20366f7", // dannyBstyle (Personal Cape)
				"5797c479-ad5a-43b0-87ca-8852d65ac639" // cheapsh0t (Personal Cape)
		));

		ILLEGAL_CAPES = ImmutableSet.copyOf(Arrays.asList(
				"http://textures.minecraft.net/texture/eec3cabfaeed5dafe61c6546297e853a547c39ec238d7c44bf4eb4a49dc1f2c0", // Mojang
				"http://textures.minecraft.net/texture/43a51d34b076f9ada555dca562206bd942e46a3c4d5f83c2c29e5b9c3d7dbcb", // Realms
				"http://textures.minecraft.net/texture/2ffda25cf1a4ed8996b767c8d16d450ba22fee7b5e416299f88a65ec5a", // Translator
				"http://textures.minecraft.net/texture/f8b55ca322e64a381b6484dac2d8aa42c78c6129336ea3ef4596f1d31b27ef", // Mojira Mod
				"http://textures.minecraft.net/texture/1672c9f13ece9c4f39a96fe22638ecd513fbe7099ca4354d3176d3793d8e9c7", // Cobalt
				"http://textures.minecraft.net/texture/86e841dcb6465d1f95a56270243d23c596da4721acd9ca2d95927b1b8535dc54", // Scrolls
				"http://textures.minecraft.net/texture/c9c058adf4a2526aa5493cf6fe37f5dbdfde7b3d4fe4df982b7bee8329e64bd", // Translator (Chinese)
				"http://textures.minecraft.net/texture/eec3cabfaeed5dafe61c6546297e853a547c39ec238d7c44bf4eb4a49dc1f2c0", // Mojang (Old)
				"http://textures.minecraft.net/texture/2897938eb320cfd8eed6fd75d42db7a9f8e2e4a3c8da1c91f6f8e1ff18c5f4", // cheapsh0t
				"http://textures.minecraft.net/texture/1658fd5989db3caffdeae2a5a70b2d0a531a7fae7401e7caef7645bccf3c", // dannyBstyle
				"http://textures.minecraft.net/texture/3d991748ae6e1cfe10f34d532748b1911b1e82b5a110ae89c34f9a2295902e", // JulianClark
				"http://textures.minecraft.net/texture/ec80a225b145c812a6ef1ca29af0f3ebf02163874d1a66e53bac99965225e0", // Millionth Customer
				"http://textures.minecraft.net/texture/b8ff4a34df87fc7d8bf1bb77bd88ac34d16c3ff52985c128e71dbc3ccd19a028" // MrMessiah
				// "//textures.minecraft.net/texture/?", // Drullkus??
		));
	}

	public static final String ORIGINAL_UUID_KEY = "originalUUID";

	private CoreClientManager _clients = require(CoreClientManager.class);
	private DisguiseManager _disguise = require(DisguiseManager.class);
	private Punish _punish = require(Punish.class);
	// private CosmeticManager _cosmetics = require(CosmeticManager.class);
	private PreferencesManager _prefs = require(PreferencesManager.class);

	private RedisDataRepository<DisguisePlayerBean> _redis;

	// The list of usernames which cannot join because someone else is joining
	// Values expire in 30 seconds if they haven't been properly cleaned up
	private Set<String> _cannotJoin = Collections.synchronizedSet(new ExpiringSet<>(1, TimeUnit.MINUTES));
	private Set<String> _loggingIn = Collections.synchronizedSet(new ExpiringSet<>(1, TimeUnit.MINUTES));

	private Set<UUID> _pendingDisguise1 = new HashSet<>();

	private PlayerDisguiseManager()
	{
		super("Player Disguise Manager");

		_serverName = _plugin.getConfig().getString("serverstatus.name");

		require(PacketHandler.class).addPacketHandler(this, PacketHandler.ListenerPriority.LOW, PacketPlayOutPlayerInfo.class, PacketPlayOutNamedEntitySpawn.class);

		_redis = new RedisDataRepository<>(Region.ALL, DisguisePlayerBean.class, "disguisedPlayer");
		
		generatePermissions();
	}
	
	private void generatePermissions()
	{
		PermissionGroup.STM.setPermission(Perm.USE_DISGUISE, false, true);
		PermissionGroup.CONTENT.setPermission(Perm.USE_DISGUISE, true, true);
		PermissionGroup.CONTENT.setPermission(Perm.BLOCKED_DISGUISE, true, true);
		PermissionGroup.BUILDER.setPermission(Perm.BLOCKED_DISGUISE, true, true);
		PermissionGroup.ADMIN.setPermission(Perm.SHORT_DISGUISE, true, true);
	}

	@Override
	public void addCommands()
	{
		addCommand(new DisguiseCommand(this));
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void cleanup(PlayerQuitEvent event)
	{
		_cannotJoin.remove(event.getPlayer().getName().toLowerCase());
		_loggingIn.remove(event.getPlayer().getName().toLowerCase());
	}

	@EventHandler(priority = EventPriority.HIGHEST)
	public void onDisguiserJoin(PlayerLoginEvent event)
	{
		for (DisguisePlayer disguisePlayer : _disguises.values())
		{
			if (disguisePlayer.getProfile().getName().equalsIgnoreCase(event.getPlayer().getName()))
			{
				event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
				event.setKickMessage("Failed to login: The authentication servers are currently down for maintenance");
				return;
			}
		}

		if (_cannotJoin.contains(event.getPlayer().getName().toLowerCase()))
		{
			event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
			event.setKickMessage("Failed to login: The authentication servers are currently down for maintenance");
			return;
		}

		CoreClient client = _clients.Get(event.getPlayer().getUniqueId());

		DisguisePlayerBean bean = _redis.getElement(client.getAccountId() + client.getName());

		if (bean != null)
		{
			Player player = Bukkit.getPlayerExact(bean.getDisguisedPlayer());
			if (player != null)
			{
				event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
				event.setKickMessage("You cannot join this server because you are disguised and the user is online!");
				return;
			}

			if (_loggingIn.contains(bean.getDisguisedPlayer()))
			{
				event.setResult(PlayerLoginEvent.Result.KICK_OTHER);
				event.setKickMessage("You cannot join this server because you are disguised and the user is currently logging in!");
				return;
			}

			_cannotJoin.add(bean.getDisguisedPlayer().toLowerCase());

			runSyncLater(() ->
			{
				_cannotJoin.remove(bean.getDisguisedPlayer().toLowerCase());
			}, 800L);
		}
		else
		{
			_loggingIn.add(event.getPlayer().getName().toLowerCase());

			runSyncLater(() -> _loggingIn.remove(event.getPlayer().getName().toLowerCase()), 800L);
		}
	}

	@EventHandler(priority = EventPriority.LOW)
	public void onDisguisedPlayerJoin(PlayerJoinEvent event)
	{
		CoreClient client = _clients.Get(event.getPlayer());
		
		if (!client.hasPermission(Perm.USE_DISGUISE))
		{
			return;
		}

		if (_redis.elementExists(client.getAccountId() + client.getName()))
		{
			DisguisePlayerBean bean = _redis.getElement(client.getAccountId() + client.getName());

			if (bean.getGameProfile() != null)
			{
				_pendingDisguise1.add(event.getPlayer().getUniqueId());

				runSyncLater(() ->
				{
					UtilPlayer.message(event.getPlayer(), F.main(getName(), "Attempting to disguise you as " + bean.getGameProfile().getName()));
					tryDisguise(event.getPlayer(), bean.getGameProfile(), () -> { });
				}, 1);
			}
		}
	}

	@EventHandler
	public void onJoinMessage(JoinMessageBroadcastEvent event)
	{
		if (_pendingDisguise1.contains(event.getPlayer().getUniqueId()))
		{
			CoreClient client = _clients.Get(event.getPlayer());
			DisguisePlayerBean bean = _redis.getElement(client.getAccountId() + client.getName());

			event.setUsername(bean.getDisguisedPlayer());
		}
	}

	public void storeDisguiseData(Player player, String disguisedName, GameProfile gameProfile)
	{
		CoreClient client = _clients.Get(player);
		_redis.addElement(new DisguisePlayerBean(client.getAccountId(), client.getName(), disguisedName, null, gameProfile), 60 * 60 * 12); // 12 hours
		System.out.println("+=+=+=+=+=+=+");
		System.out.println("ADDING DISGUISE INFO FOR " + player.getName());
		System.out.println("+=+=+=+=+=+=+");
	}

	public void removeDisguiseData(Player player)
	{
		CoreClient client = _clients.Get(player);
		_redis.removeElement(client.getAccountId() + client.getName());
		System.out.println("+=+=+=+=+=+=+");
		System.out.println("REMOVING DISGUISE INFO FOR " + player.getName());
		System.out.println("+=+=+=+=+=+=+");
	}


	/*
	 * Maps players (their UUID) with their disguises
	 */
	private final Map<UUID, DisguisePlayer> _disguises = new HashMap<>();

	/*
	 * Mapping of old username to disguised username
	 */
	private final Map<String, String> _mapping = new HashMap<>();

	/*
	 * A set of usernames which are currently being disguised
	 * This is to prevent two people from running /disguise at the same time
	 */
	private Set<String> _pendingDisguise = Collections.synchronizedSet(new HashSet<>());

	private final String _serverName;

	private RedisDataRepository<PlayerStatus> _repository = new RedisDataRepository<>(ServerManager.getMasterConnection(), ServerManager.getSlaveConnection(),
			Region.currentRegion(), PlayerStatus.class, "playerStatus");

	@EventHandler
	public void onQuit(PlayerQuitEvent event)
	{
		_pendingDisguise1.remove(event.getPlayer().getUniqueId());
		_disguises.remove(event.getPlayer().getUniqueId());
		_mapping.remove(event.getPlayer().getName().toLowerCase());
	}

	private boolean isDisguised(Player player)
	{
		return _disguises.containsKey(player.getUniqueId());
	}

	public void allow(Player player)
	{
		_pendingDisguise1.remove(player.getUniqueId());
	}

	public DisguiseManager getDisguiseManager()
	{
		return this._disguise;
	}

	public CoreClientManager getClientManager()
	{
		return this._clients;
	}

	public Punish getPunishManager()
	{
		return this._punish;
	}

	public PreferencesManager getPreferencesManager()
	{
		return this._prefs;
	}

	// DisguiseManager is too slow, we need to cancel it here first
	@Override
	public void handle(PacketInfo packetInfo)
	{
		if (packetInfo.getPacket() instanceof PacketPlayOutPlayerInfo)
		{
			PacketPlayOutPlayerInfo packet = (PacketPlayOutPlayerInfo) packetInfo.getPacket();
			Iterator<PacketPlayOutPlayerInfo.PlayerInfoData> iterator = packet.b.iterator();
			while (iterator.hasNext())
			{
				PacketPlayOutPlayerInfo.PlayerInfoData data = iterator.next();
				if (_pendingDisguise1.contains(data.a().getId()))
				{
					iterator.remove();
				}
			}

			if (packet.b.isEmpty())
			{
				packetInfo.setCancelled(true);
			}
			else
			{
				if (packet.a != PacketPlayOutPlayerInfo.EnumPlayerInfoAction.ADD_PLAYER && packet.a != PacketPlayOutPlayerInfo.EnumPlayerInfoAction.REMOVE_PLAYER)
				{
					for (PacketPlayOutPlayerInfo.PlayerInfoData data : packet.b)
					{
						Player player = Bukkit.getPlayerExact(data.a().getName());

						// Would be null during login, we don't care
						// LoginListener -> PlayerList#a(NetworkManager, EntityPlayer)
						//     -> PlayerList#a(EntityPlayer, EntityPlayer, WorldServer) (Sends this packet)
						//     -> PlayerList#onPlayerJoin(EntityPlayer, String) (Populates Bukkit.getPlayerExact)
						if (player != null)
						{
							if (isDisguised(player))
							{
								PacketPlayOutPlayerInfo custom = new PacketPlayOutPlayerInfo();
								custom.a = packet.a;

								GameProfile originalProfile = _disguises.get(player.getUniqueId()).getOriginalProfile();
								custom.b.add(custom.new PlayerInfoData(originalProfile, data.b, data.c, data.d()));

								((CraftPlayer) player).getHandle().playerConnection.networkManager.handle(custom);
							}
						}
					}
				}
			}
		}
		else if (packetInfo.getPacket() instanceof PacketPlayOutNamedEntitySpawn)
		{
			PacketPlayOutNamedEntitySpawn spawn = (PacketPlayOutNamedEntitySpawn) packetInfo.getPacket();
			if (_pendingDisguise1.contains(spawn.b))
			{
				packetInfo.setCancelled(true);
			}
		}
	}

	public String getRealName(Player caller)
	{
		CoreClient coreClient = _clients.Get(caller);
		return coreClient == null ? caller.getName() : coreClient.getName();
	}

	public void undisguise(Player caller)
	{
		if (!isDisguised(caller))
		{
			if (getDisguiseManager().isDisguised(caller))
			{
				UtilPlayer.message(caller, F.main("Disguise", "You have been disguised by something else. Perhaps you are morphed?"));
			}
			else
			{
				UtilPlayer.message(caller, F.main("Disguise", "You are not disguised. You can disguise with /disguise <username> [username of skin]"));
			}
			return;
		}

		if (getDisguiseManager().getActiveDisguise(caller) != _disguises.get(caller.getUniqueId()))
		{
			UtilPlayer.message(caller, F.main("Disguise", "Could not undisguise as it is not your active disguise. Perhaps you are morphed?"));
			return;
		}

		PlayerPreUndisguiseEvent playerPreUndisguiseEvent = new PlayerPreUndisguiseEvent(caller);
		UtilServer.CallEvent(playerPreUndisguiseEvent);
		if (playerPreUndisguiseEvent.isCancelled())
		{
			return;
		}

		DisguisePlayer disguise = _disguises.remove(caller.getUniqueId());

		undisguise(caller, disguise);

		_mapping.remove(disguise.getName().toLowerCase());

		UtilPlayer.message(caller, F.main("Disguise", "You are no longer disguised!"));
		getPluginManager().callEvent(new PlayerUndisguisedEvent(caller));
		removeDisguiseData(caller);
	}

	public void undisguise(Player caller, DisguisePlayer disguise)
	{
		GameProfile originalProfile = disguise.getOriginalProfile();
		GameProfile currentProfile = ((CraftPlayer) caller).getProfile();
		boolean sameName = caller.getName().equals(currentProfile.getName());

		if (!sameName)
		{
			require(ScoreboardManager.class).handlePlayerQuit(disguise.getName());
		}

		try
		{
			UtilGameProfile.changeName(currentProfile, originalProfile.getName());
			UtilGameProfile.changeId(currentProfile, originalProfile.getId());
			currentProfile.getProperties().clear();
			currentProfile.getProperties().putAll(originalProfile.getProperties());

			Field playersByName = PlayerList.class.getDeclaredField("playersByName");
			playersByName.setAccessible(true);
			Map map = (Map) playersByName.get(MinecraftServer.getServer().getPlayerList());
			map.remove(disguise.getProfile().getName());
			map.put(disguise.getOriginalProfile().getName(), disguise.getEntity());
		}
		catch (Exception ex)
		{
			UtilPlayer.message(caller, F.main("Disguise", "Could not undisguise because something went terribly wrong :("));
			ex.printStackTrace();
			return;
		}

		getDisguiseManager().undisguise(disguise);

		GameProfile disguisedProfile = disguise.getProfile();

		CoreClient client = getClientManager().Get(caller);
		client.undisguise();

		require(FriendManager.class).updatePlayerStatus(disguisedProfile.getId(), null);
		require(FriendManager.class).updatePlayerStatus(originalProfile.getId(), new PlayerStatus(originalProfile.getId(), originalProfile.getName(), _serverName));

		if (!sameName)
		{
			require(ScoreboardManager.class).handlePlayerJoin(disguise.getOriginalProfile().getName());
		}
	}

	public void tryDisguise(Player caller, GameProfile requestedProfile, Runnable onComplete)
	{
		if (getDisguiseManager().isDisguised(caller))
		{
			if (isDisguised(caller))
			{
				UtilPlayer.message(caller,
						F.main("Disguise", "You are already disguised. Please undisguise by using /disguise"));
			} else
			{
				UtilPlayer.message(caller, F.main("Disguise", "You are already disguised. Perhaps you are morphed?"));
			}
			return;
		}

		if (isDisguised(caller))
		{
			UtilPlayer.message(caller,
					F.main("Disguise", "You are already disguised. Please undisguise by using /disguise"));
			return;
		}

		CoreClient callerClient = getClientManager().Get(caller);

		String requestedUsername = requestedProfile.getName();
		if (requestedUsername.equalsIgnoreCase(caller.getName()))
		{
			if (doDisguise(caller, requestedProfile, callerClient, callerClient))
			{
				onComplete.run();
			}
			return;
		}

		for (Player other : UtilServer.getPlayersCollection())
		{
			if (other.getName().equalsIgnoreCase(requestedUsername))
			{
				UtilPlayer.message(caller, C.cRed + F.main("Disguise", "This name is already in use!"));
				return;
			}
		}

		if (_pendingDisguise.contains(requestedUsername.toLowerCase()))
		{
			UtilPlayer.message(caller, F.main("Disguise", "Someone is already disguising as that user"));
			return;
		}

		getClientManager().getOrLoadClient(requestedUsername, other ->
		{
			if (other != null)
			{
				if (other.hasPermission(Perm.BLOCKED_DISGUISE))
				{
					UtilPlayer.message(caller,
							F.main("Disguise", "You can't disguise as that person!"));
					return;
				}

				PunishClient pclient = getPunishManager().GetClient(requestedUsername);
				if (pclient != null && (pclient.IsBanned() || pclient.IsMuted()))
				{
					UtilPlayer.message(caller,
							F.main("Disguise", "You can't disguise as players who are banned/muted!"));
					return;
				}
			}

			if (doDisguise(caller, requestedProfile, callerClient, other))
			{
				onComplete.run();
			}
		});
	}

	public boolean doDisguise(Player caller, GameProfile requestedProfile, CoreClient callerClient, CoreClient otherClient)
	{
		String requestedUsername = requestedProfile.getName();
		_pendingDisguise.add(requestedUsername.toLowerCase());

		if (!requestedUsername.equalsIgnoreCase(caller.getName()))
		{
			_cannotJoin.add(requestedUsername.toLowerCase());
		} else
		{
			DisguisePlayer disguisePlayer = new DisguisePlayer(caller, requestedProfile);
			disguisePlayer.showInTabList(true, 0);
			allow(caller);
			getDisguiseManager().disguise(disguisePlayer, () ->
			{
				((CraftPlayer) caller).getProfile().getProperties().clear();
				((CraftPlayer) caller).getProfile().getProperties().putAll(disguisePlayer.getProfile().getProperties());

				storeDisguiseData(caller, caller.getName(), requestedProfile);

				_disguises.put(caller.getUniqueId(), disguisePlayer);

				_pendingDisguise.remove(requestedUsername.toLowerCase());
			});

			return true;
		}

		PlayerPreDisguiseEvent playerPreDisguiseEvent = new PlayerPreDisguiseEvent(caller, requestedUsername);
		UtilServer.CallEvent(playerPreDisguiseEvent);
		if (playerPreDisguiseEvent.isCancelled())
		{
			UtilPlayer.message(caller, F.main(getName(), "Your disguise was cancelled by something"));
			_pendingDisguise.remove(requestedUsername.toLowerCase());
			_cannotJoin.remove(requestedUsername.toLowerCase());
			return false;
		}

		PermissionGroup otherRank = otherClient != null ? otherClient.getPrimaryGroup() : PermissionGroup.PLAYER;
		callerClient.disguise(requestedUsername, requestedProfile.getId(), otherRank);

		_mapping.put(callerClient.getDisguisedAs().toLowerCase(), callerClient.getName());

		System.out.println("=================");
		System.out.println("Disguising " + caller.getName() + " as:");
		System.out.println(requestedProfile.getName() + " id " + requestedProfile.getId());
		System.out.println("Properties:");
		for (Map.Entry<String, Property> p : requestedProfile.getProperties().entries())
		{
			System.out.println("\t" + p.getKey() + " " + p.getValue().getName());
			System.out.println("\t" + p.getValue().getValue());
			System.out.println("\t" + p.getValue().getSignature());
		}
		System.out.println("=================");

		DisguisePlayer disguisePlayer = new DisguisePlayer(caller, requestedProfile);
		disguisePlayer.showInTabList(true, 0);
		allow(caller);
		getDisguiseManager().disguise(disguisePlayer, () ->
		{
			GameProfile callerProfile = ((CraftPlayer) caller).getProfile();

			require(ScoreboardManager.class).handlePlayerQuit(disguisePlayer.getOriginalProfile().getName());

			try
			{
				UtilGameProfile.changeName(callerProfile, disguisePlayer.getProfile().getName());
				UtilGameProfile.changeId(callerProfile, disguisePlayer.getProfile().getId());

				Field playersByName = PlayerList.class.getDeclaredField("playersByName");
				playersByName.setAccessible(true);
				Map map = (Map) playersByName.get(MinecraftServer.getServer().getPlayerList());
				map.remove(disguisePlayer.getOriginalProfile().getName());
				map.put(disguisePlayer.getProfile().getName(), disguisePlayer.getEntity());
			} catch (Throwable t)
			{
				t.printStackTrace();
			}

			require(ScoreboardManager.class).handlePlayerJoin(disguisePlayer.getName());

			callerProfile.getProperties().clear();
			callerProfile.getProperties().putAll(disguisePlayer.getProfile().getProperties());

			callerProfile.getProperties().removeAll(ORIGINAL_UUID_KEY);
			callerProfile.getProperties()
			             .put(ORIGINAL_UUID_KEY, new Property(ORIGINAL_UUID_KEY, caller.getUniqueId().toString()));

			require(FriendManager.class).updatePlayerStatus(disguisePlayer.getOriginalProfile().getId(), null);
			require(FriendManager.class).updatePlayerStatus(disguisePlayer.getProfile().getId(),
					new PlayerStatus(disguisePlayer.getProfile().getId(), requestedUsername, _serverName));

			_disguises.put(caller.getUniqueId(), disguisePlayer);

			UtilPlayer.message(caller, F.main("Disguise", "Disguise Active: " + ChatColor.RESET + requestedUsername));

			UtilServer.CallEvent(new PlayerDisguisedEvent(caller));

			storeDisguiseData(caller, requestedUsername, requestedProfile);

			_pendingDisguise.remove(requestedUsername.toLowerCase());

			_cannotJoin.remove(requestedUsername.toLowerCase());
		});

		return true;
	}

	public void tryDisguise(Player caller, String requestedUsername, String requestedSkin, Runnable onComplete)
	{
		if (!validateUsername(caller, requestedUsername, true)) return;
		if (!validateUsername(caller, requestedSkin, false)) return;

		UtilGameProfile.getProfileByName(requestedUsername, true, requestedProfile ->
		{
			if (VERY_SPECIAL_PEOPLE.contains(requestedProfile.getId().toString().toLowerCase()))
			{
				UtilPlayer.message(caller, F.main("Disguise", "The chosen username of " + F.elem(requestedUsername) + " is not valid"));
				return;
			}

			if (!verifyProfile(caller, requestedProfile)) return;

			Consumer<GameProfile> skinConsumer = requestedProfileSkin ->
			{
				if (!verifyProfile(caller, requestedProfileSkin)) return;

				SkinData skinData = SkinData.constructFromGameProfile(requestedProfileSkin, true, true);
				requestedProfile.getProperties().clear();
				requestedProfile.getProperties().put("textures", skinData.getProperty());

				tryDisguise(caller, requestedProfile, onComplete);
			};

			if (!requestedUsername.equalsIgnoreCase(requestedSkin))
			{
				UtilGameProfile.getProfileByName(requestedSkin, true, skinConsumer);
			}
			else
			{
				skinConsumer.accept(UtilGameProfile.clone(requestedProfile));
			}
		});
	}

	private boolean validateUsername(Player caller, String username, boolean isUsername)
	{
		String replaced = UtilGameProfile.legalize(username);
		if (!replaced.equals(username))
		{
			UtilPlayer.message(caller, F.main("Disguise", "The chosen username of " + F.elem(username) + " is not valid"));
			return false;
		}

		if (username.length() > 16)
		{
			UtilPlayer.message(caller, F.main("Disguise", "The chosen username of " + F.elem(username) + " is " + F.count(String.valueOf(username.length() - 16)) + " characters too long!"));
			return false;
		}

		if (username.length() <= 0)
		{
			UtilPlayer.message(caller, F.main("Disguise", "The chosen username of " + F.elem(username) + " must be longer than " + F.count("0") + " characters"));
			return false;
		}

		if (isUsername)
		{
			if (ILLEGAL_USERNAMES.contains(username.toLowerCase()) || MOJANG.contains(username.toLowerCase()))
			{
				if (!UtilServer.isTestServer())
				{
					UtilPlayer.message(caller, F.main("Disguise", "The chosen username of " + F.elem(username) + " is not valid"));
					return false;
				}
			}
			if (username.length() < 3)
			{
				if (!_clients.Get(caller).hasPermission(Perm.SHORT_DISGUISE))
				{
					UtilPlayer.message(caller, F.main("Disguise", "The chosen username of " + F.elem(username) + " must be longer than " + F.count("2") + " letters"));
					return false;
				}
			}
		}

		return true;
	}

	private boolean verifyProfile(Player caller, GameProfile requestedProfile)
	{
		Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> map = MinecraftServer.getServer().aD().getTextures(requestedProfile, false);

		if (map.containsKey(MinecraftProfileTexture.Type.CAPE))
		{
			MinecraftProfileTexture texture = map.get(MinecraftProfileTexture.Type.CAPE);
			if (ILLEGAL_CAPES.contains(texture.getUrl().toLowerCase()))
			{
				if (!UtilServer.isTestServer())
				{
					UtilPlayer.message(caller, F.main("Disguise", "The chosen username of " + F.elem(requestedProfile.getName()) + " is not valid"));
					return false;
				}
			}
		}
		return true;
	}
}