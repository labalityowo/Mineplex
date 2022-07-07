package mineplex.core.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.UUID;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.MinecraftServer;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mojang.authlib.GameProfile;

import mineplex.core.Managers;
import mineplex.core.profileCache.ProfileCacheManager;
import mineplex.core.thread.ThreadPool;

public class UtilGameProfile
{
	private static final Cache<UUID, Boolean> TEXTURES = CacheBuilder.newBuilder()
			.expireAfterWrite(5, TimeUnit.MINUTES)
			.concurrencyLevel(4)
			.build();

	/**
	 * Get a {@link GameProfile} given a username.
	 * <p>
	 * If you desperately must block the current thread, you may pass a null Consumer and use Future.get()
	 *
	 * @param username The username of the player
	 * @param nonNull  If true, an OfflinePlayer GameProfile will be returned should the username not be valid
	 * @param fetched  The Consumer which will receive the GameProfile instance. This Consumer will not be called on the main thread
	 * @return The GameProfile - always an unique instance
	 */
	public static Future<GameProfile> getProfileByName(String username, boolean nonNull, Consumer<GameProfile> fetched)
	{
		return ThreadPool.ASYNC.submit(() ->
		{
			// First, try to load it from the redis cache
			GameProfile gameProfile = Managers.get(ProfileCacheManager.class).attemptToLoadProfile(username);

			if (gameProfile == null)
			{
				gameProfile = MinecraftServer.getServer().getUserCache().getProfile(username);

				// This profile is not guaranteed to be unique, and we don't want to be modifying the cache...
				if (gameProfile != null)
				{
					gameProfile = clone(gameProfile);
				}
			}

			// We've found the profile
			if (gameProfile != null)
			{
				if (!gameProfile.getProperties().containsKey("textures"))
				{
					// There are no textures in the GameProfile and we have not yet tried to load it
					if (TEXTURES.getIfPresent(gameProfile.getId()) == null)
					{
						TEXTURES.put(gameProfile.getId(), true);

						// Try to fill it and cache the profile
						MinecraftServer.getServer().aD().fillProfileProperties(gameProfile, true);
						Managers.get(ProfileCacheManager.class).cacheProfile(gameProfile);
					}
				}
			}

			// If a nonnull GameProfile is requested, then we'll use the default OfflinePlayer UUID format
			if (gameProfile == null && nonNull)
			{
				gameProfile = new GameProfile(EntityPlayer.b(username), username);
			}

			// Clone it one last time in case some Mojang API changes and we're left scratching our heads
			// wondering why everything suddenly broke
			gameProfile = clone(gameProfile);

			if (fetched != null)
			{
				fetched.accept(gameProfile);
			}

			return gameProfile;
		});
	}

	/**
	 * Clones a GameProfile
	 *
	 * @param input The GameProfile to clone
	 * @return A copy of the GameProfile
	 */
	public static GameProfile clone(GameProfile input)
	{
		GameProfile newProfile = new GameProfile(input.getId(), input.getName());
		newProfile.getProperties().putAll(input.getProperties());
		return newProfile;
	}

	// Pattern to remove all non alphanumeric + underscore letters
	private static final Pattern LEGAL_USERNAME = Pattern.compile("[^a-z0-9_]", Pattern.CASE_INSENSITIVE);

	/**
	 * Convert a string to a legal username equivalent
	 *
	 * @param in The original username
	 * @returns A legal version of the username (with illegal characters stripped out
	 */
	public static String legalize(String in)
	{
		return LEGAL_USERNAME.matcher(in).replaceAll("");
	}

	private static final Field GAME_PROFILE_NAME_FIELD;
	private static final Field GAME_PROFILE_ID_FIELD;
	private static final Field SKULL_META_PROFILE_FIELD;

	static
	{
		try
		{
			GAME_PROFILE_NAME_FIELD = GameProfile.class.getDeclaredField("name");
			GAME_PROFILE_NAME_FIELD.setAccessible(true);
			GAME_PROFILE_ID_FIELD = GameProfile.class.getDeclaredField("id");
			GAME_PROFILE_ID_FIELD.setAccessible(true);
			SKULL_META_PROFILE_FIELD = Class.forName("org.bukkit.craftbukkit.v1_8_R3.inventory.CraftMetaSkull").getDeclaredField("profile");
			SKULL_META_PROFILE_FIELD.setAccessible(true);

			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(GAME_PROFILE_NAME_FIELD, GAME_PROFILE_NAME_FIELD.getModifiers() & ~Modifier.FINAL);
			modifiersField.setInt(GAME_PROFILE_ID_FIELD, GAME_PROFILE_ID_FIELD.getModifiers() & ~Modifier.FINAL);

			GameProfile testProfile = new GameProfile(UUID.randomUUID(), "Testing");
			String changedName = "TestSuccessful";
			UUID changedUUID = UUID.randomUUID();
			changeName(testProfile, changedName);
			changeId(testProfile, changedUUID);
			if (!testProfile.getName().equals(changedName))
			{
				throw new RuntimeException("Could not change name of test GameProfile: Got " + testProfile.getName() + ", expected " + changedName);
			}
			if (!testProfile.getId().equals(changedUUID))
			{
				throw new RuntimeException("Could not change UUID of test GameProfile: Got " + testProfile.getId() + ", expected " + changedUUID);
			}
		}
		catch (ReflectiveOperationException ex)
		{
			throw new RuntimeException(ex);
		}
	}

	public static void changeName(GameProfile gameProfile, String changedName) throws ReflectiveOperationException
	{
		GAME_PROFILE_NAME_FIELD.set(gameProfile, changedName);
	}

	public static void changeId(GameProfile gameProfile, UUID newUUID) throws ReflectiveOperationException
	{
		GAME_PROFILE_ID_FIELD.set(gameProfile, newUUID);
	}

	/**
	 * Gets the GameProfile of a player, cloned
	 */
	public static GameProfile getGameProfile(Player player)
	{
		return clone(((CraftPlayer) player).getProfile());
	}

	public static void setGameProfile(Player player, ItemStack stack)
	{
		setGameProfile(getGameProfile(player), stack);
	}

	public static void setGameProfile(GameProfile profile, ItemStack stack)
	{
		ItemMeta meta = stack.getItemMeta();
		if (meta instanceof SkullMeta)
		{
			SkullMeta skullMeta = (SkullMeta) meta;
			try
			{
				SKULL_META_PROFILE_FIELD.set(skullMeta, profile);
			}
			catch (IllegalAccessException e)
			{
				e.printStackTrace();
			}

			stack.setItemMeta(skullMeta);
		}
	}
}
