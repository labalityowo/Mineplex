package mineplex.core.profileCache;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import mineplex.core.ReflectivelyCreateMiniPlugin;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.CraftServer;
import org.bukkit.event.EventHandler;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.authlib.Agent;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.GameProfileRepository;
import com.mojang.authlib.ProfileLookupCallback;
import com.mojang.authlib.properties.PropertyMap;
import com.mojang.authlib.yggdrasil.YggdrasilGameProfileRepository;
import com.mojang.authlib.yggdrasil.response.ProfileSearchResultsResponse;
import com.mojang.util.UUIDTypeAdapter;

import mineplex.core.MiniPlugin;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.serverdata.Region;
import mineplex.serverdata.redis.RedisDataRepository;
import mineplex.serverdata.servers.ServerManager;

@ReflectivelyCreateMiniPlugin
public class ProfileCacheManager extends MiniPlugin implements GameProfileRepository
{
	private YggdrasilGameProfileRepository _mojangProfileRepository;
	private RedisDataRepository<ProfileData> _profileRepository;
	private Gson _gson;

	private ProfileCacheManager()
	{
		super("Profile Cache");

		_profileRepository = new RedisDataRepository<ProfileData>(ServerManager.getMasterConnection(), ServerManager.getSlaveConnection(),
				Region.ALL, ProfileData.class, "profileCacheRepo");

		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(PropertyMap.class, new PropertyMap.Serializer());
		builder.registerTypeAdapter(UUID.class, new UUIDTypeAdapter());
		builder.registerTypeAdapter(ProfileSearchResultsResponse.class, new com.mojang.authlib.yggdrasil.response.ProfileSearchResultsResponse.Serializer());
		_gson = builder.create();

		try
		{
			Field mojangProfileRepo = net.minecraft.server.v1_8_R3.MinecraftServer.class.getDeclaredField("Y");
			
			mojangProfileRepo.setAccessible(true);
			
			_mojangProfileRepository = (YggdrasilGameProfileRepository) mojangProfileRepo.get(((CraftServer)Bukkit.getServer()).getServer());
			mojangProfileRepo.set(((CraftServer)Bukkit.getServer()).getServer(), this);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			System.out.println("================================================");
			System.out.println("Failed to load Profile Cache (Skins)");
			System.out.println("Are you using the correct modified Craftbukkit?");
			System.out.println("================================================");
		}
	}

	/**
	 * Tries to get a GameProfile; guaranteed to be an unique instance
	 */
	public GameProfile attemptToLoadProfile(String playerName)
	{
		ProfileData profile = _profileRepository.getElement(playerName.toLowerCase());

		if (profile != null)
		{
			PropertyMap propertyMap = _gson.fromJson(profile.getPropertyMap(), PropertyMap.class);
			GameProfile gameProfile = new GameProfile(profile.getUuid(), profile.getPlayerName());
			gameProfile.getProperties().putAll(propertyMap);
			return gameProfile;
		}

		return null;
	}

	public void cacheProfile(final GameProfile profile)
	{
		if (Bukkit.isPrimaryThread())
		{
			runAsync(new Runnable()
			{
				public void run()
				{
					cacheProfileSafely(profile);
				}
			});
		}
		else
			cacheProfileSafely(profile);
	}

	@Override
	public void findProfilesByNames(String[] profileNames, Agent agent, ProfileLookupCallback profileLookupCallback) 
	{
		List<String> uncachedProfileNames = new ArrayList<>();
		
		for (String profileName : profileNames)
		{
			GameProfile profile = attemptToLoadProfile(profileName);
			
			if (profile == null)
				uncachedProfileNames.add(profileName);
			else
				profileLookupCallback.onProfileLookupSucceeded(profile);
		}
		
		_mojangProfileRepository.findProfilesByNames(uncachedProfileNames.toArray(new String[uncachedProfileNames.size()]), agent, new ProfileCacheLookupCallback(this, profileLookupCallback));
	}
	
	@EventHandler
	public void clearRepository(UpdateEvent event)
	{
		if (event.getType() != UpdateType.MIN_10)
			return;
		
		_profileRepository.clean();
	}
	
	private void cacheProfileSafely(GameProfile profile)
	{
		ProfileData data = new ProfileData(profile.getId(), profile.getName(), _gson.toJson(profile.getProperties()));
		_profileRepository.addElement(data, 60 * 60 * 24); // 1 day
	}
}
