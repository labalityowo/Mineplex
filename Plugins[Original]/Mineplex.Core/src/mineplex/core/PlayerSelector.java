package mineplex.core;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import mineplex.core.account.CoreClient;
import mineplex.core.account.CoreClientManager;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.UtilMath;
import mineplex.core.incognito.IncognitoManager;

/**
 * A helper for selecting arbitrary players given a set of conditions
 */
public class PlayerSelector
{
	/**
	 * Select a random player from all the online players
	 *
	 * @return A random player, or null if none matched
	 */
	public static Player selectPlayer()
	{
		return selectPlayer(player -> true);
	}

	/**
	 * Select a random player who match the given criterion
	 *
	 * @param selector The condition that the players must match against
	 * @return A random player, or null if none matched
	 */
	public static Player selectPlayer(Predicate<Player> selector)
	{
		List<Player> selected = selectPlayers(selector);
		return selected.get(ThreadLocalRandom.current().nextInt(selected.size()));
	}

	/**
	 * Select all the players who match the given criterion
	 * @param selector The condition that the players must match against
	 * @return All the players who match
	 */
	public static List<Player> selectPlayers(Predicate<Player> selector)
	{
		return Bukkit.getOnlinePlayers().stream().filter(selector).collect(Collectors.<Player>toList());
	}

	/**
	 * This condition will return true if the player is not vanished, and false if they are.
	 *
	 * If the {@link IncognitoManager} is not loaded, then this will return true
	 */
	public static final Predicate<Player> NOT_VANISHED = player ->
	{
		IncognitoManager manager = Managers.get(IncognitoManager.class);
		if (manager == null)
		{
			return true;
		}
		return !manager.Get(player).Status;
	};

	/**
	 * This condition will return true if the player is not spectating, and false if they are
	 */
	public static final Predicate<Player> NOT_SPECTATING = player -> player.getGameMode() != GameMode.SPECTATOR;

	/**
	 * This condition will return true if the player has one of the given ranks as their primary
	 *
	 * @param useDisguisedRank Whether to use the disguised rank of the player should they be disguised
	 * @param ranks The ranks to check
	 * @return The resulting criterion
	 */
	public static Predicate<Player> hasAnyRank(boolean useDisguisedRank, PermissionGroup... groups)
	{
		return player ->
		{
			CoreClientManager coreClientManager = Managers.get(CoreClientManager.class);
			if (coreClientManager == null)
			{
				return true;
			}
			CoreClient client = coreClientManager.Get(player);
			PermissionGroup group = useDisguisedRank ? client.getRealOrDisguisedPrimaryGroup() : client.getPrimaryGroup();

			for (PermissionGroup requiredGroup : groups)
			{
				if (group == requiredGroup)
				{
					return true;
				}
			}

			return false;
		};
	}


	/**
	 * This condition will return true if the entity is in the world specified
	 * @param world The world that the entity must be in
	 * @return The resulting criterion
	 */
	public static Predicate<Player> inWorld(World world)
	{
		return entity -> world == null || entity.getWorld().equals(world);
	}

	public static Predicate<Player> within(Location center, double radius)
	{
		return player -> UtilMath.offset(player.getLocation(), center) <= radius;
	}
}