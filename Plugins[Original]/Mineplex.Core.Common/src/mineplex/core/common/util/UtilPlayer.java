package mineplex.core.common.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.EntityTracker;
import net.minecraft.server.v1_8_R3.EntityTrackerEntry;
import net.minecraft.server.v1_8_R3.Packet;
import net.minecraft.server.v1_8_R3.PacketPlayOutCustomSoundEffect;
import net.minecraft.server.v1_8_R3.PacketPlayOutNamedSoundEffect;
import net.minecraft.server.v1_8_R3.PacketPlayOutWorldBorder;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import net.minecraft.server.v1_8_R3.WorldBorder;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.event.CraftEventFactory;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

import com.mineplex.ProtocolVersion;

import mineplex.core.common.MinecraftVersion;

public class UtilPlayer
{
	private static Random RANDOM = new Random();

	// A mapping of player names (Keys) to the system time when they last changed active Hotbar Slot
	private static Map<String, Long> _hotbarUpdates = new HashMap<>();

	// A mapping of player UUIDs (Keys) to the world border they are using (if they are using)
	private static final Map<UUID, WorldBorder> WORLD_BORDERS = new HashMap<>();

	// A mapping of player UUIDs (Keys) to the list of command they're allowed
	private static final Map<UUID, List<String>> ALLOWED_COMMANDS = new HashMap<>();

	// The amount of time (in milliseconds) after changin hotbars that you can block
	public static final long BLOCKING_HOTBAR_DELAY = 100;

	// Regex used to match player names
	private static final Pattern USERNMAME_PATTERN = Pattern.compile("^([A-Za-z0-9_]{1,16}|\\$)$");

	private static boolean hasIntersection(Vector3D p1, Vector3D p2, Vector3D min, Vector3D max)
	{
		final double epsilon = 0.0001f;

		Vector3D d = p2.subtract(p1).multiply(0.5);
		Vector3D e = max.subtract(min).multiply(0.5);
		Vector3D c = p1.add(d).subtract(min.add(max).multiply(0.5));
		Vector3D ad = d.abs();

		if (Math.abs(c.x) > e.x + ad.x)
			return false;
		if (Math.abs(c.y) > e.y + ad.y)
			return false;
		if (Math.abs(c.z) > e.z + ad.z)
			return false;

		if (Math.abs(d.y * c.z - d.z * c.y) > e.y * ad.z + e.z * ad.y + epsilon)
			return false;
		if (Math.abs(d.z * c.x - d.x * c.z) > e.z * ad.x + e.x * ad.z + epsilon)
			return false;
		if (Math.abs(d.x * c.y - d.y * c.x) > e.x * ad.y + e.y * ad.x + epsilon)
			return false;

		return true;
	}

	public static void setSpectating(Player player, Entity ent)
	{
		if(!ent.isValid()) return;

		player.setGameMode(GameMode.SPECTATOR);

		if(player.getSpectatorTarget() != null)
		{
			player.setSpectatorTarget(null);
		}

		player.teleport(ent);

		if(isTracked(player, ent))
		{
			player.setSpectatorTarget(ent);
		}
		else
		{
			new BukkitRunnable()
			{
				public void run()
				{
					setSpectating(player, ent);
				}
			}.runTaskLater(UtilServer.getPlugin(), 1);
		}
	}

	/**
	 * Returns true if the given player is tracking the given target, meaning that the player
	 * got the entity loaded and knows about the entity.
	 */
	public static boolean isTracked(Player player, Entity target)
	{
		EntityPlayer ep = ((CraftPlayer) player).getHandle();

		EntityTracker tracker = ep.u().getTracker();
		EntityTrackerEntry entry = tracker.trackedEntities.get(target.getEntityId());

		return entry.trackedPlayers.contains(ep);
	}

	private static class Vector3D
	{

		// Use protected members, like Bukkit
		private final double x;
		private final double y;
		private final double z;

		private Vector3D(double x, double y, double z)
		{
			this.x = x;
			this.y = y;
			this.z = z;
		}

		private Vector3D(Location location)
		{
			this(location.toVector());
		}

		private Vector3D(Vector vector)
		{
			if (vector == null)
				throw new IllegalArgumentException("Vector cannot be NULL.");
			this.x = vector.getX();
			this.y = vector.getY();
			this.z = vector.getZ();
		}

		private Vector3D abs()
		{
			return new Vector3D(Math.abs(x), Math.abs(y), Math.abs(z));
		}

		private Vector3D add(double x, double y, double z)
		{
			return new Vector3D(this.x + x, this.y + y, this.z + z);
		}

		private Vector3D add(Vector3D other)
		{
			if (other == null)
				throw new IllegalArgumentException("other cannot be NULL");

			return new Vector3D(x + other.x, y + other.y, z + other.z);
		}

		private Vector3D multiply(double factor)
		{
			return new Vector3D(x * factor, y * factor, z * factor);
		}

		private Vector3D multiply(int factor)
		{
			return new Vector3D(x * factor, y * factor, z * factor);
		}

		private Vector3D subtract(Vector3D other)
		{
			if (other == null)
				throw new IllegalArgumentException("other cannot be NULL");
			return new Vector3D(x - other.x, y - other.y, z - other.z);
		}
	}

	public static Player getPlayerInSight(Player p, int range, boolean lineOfSight)
	{
		Location observerPos = p.getEyeLocation();
		Vector3D observerDir = new Vector3D(observerPos.getDirection());
		Vector3D observerStart = new Vector3D(observerPos);
		Vector3D observerEnd = observerStart.add(observerDir.multiply(range));

		Player hit = null;

		for (Entity entity : p.getNearbyEntities(range, range, range))
		{

			if (entity == p || UtilPlayer.isSpectator(entity) || !(entity instanceof Player))
				continue;

			double theirDist = p.getEyeLocation().distance(entity.getLocation());

			if (lineOfSight
					&& p.getLastTwoTargetBlocks(UtilBlock.blockAirFoliageSet, (int) Math.ceil(theirDist)).get(0).getLocation()
					.distance(p.getEyeLocation()) + 1 < theirDist)
				continue;

			Vector3D targetPos = new Vector3D(entity.getLocation());
			Vector3D minimum = targetPos.add(-0.5, 0, -0.5);
			Vector3D maximum = targetPos.add(0.5, 1.67, 0.5);

			if (hasIntersection(observerStart, observerEnd, minimum, maximum))
			{
				if (hit == null
						|| hit.getLocation().distanceSquared(observerPos) > entity.getLocation().distanceSquared(observerPos))
				{
					hit = (Player) entity;
				}
			}
		}
		return hit;
	}

	/**
	 * @param player - the player to be checked for blocking status
	 * @return true, if the {@code player} is blocking and has not recently
	 * changed hotbar slots (within {@value BLOCKING_HOTBAR_DELAY} milliseconds), false otherwise.
	 */
	public static boolean isBlocking(Player player)
	{
		String name = player.getName();
		long lastUpdate = _hotbarUpdates.containsKey(name) ? _hotbarUpdates.get(name) : 0;;
		long duration = System.currentTimeMillis() - lastUpdate;

		return player.isBlocking();// && UtilItem.isSword(player.getItemInHand())
		//&& duration >= BLOCKING_HOTBAR_DELAY;
	}

	/**
	 * Mark the {@code player} as having changed hotbar slots.
	 * @param player - the player to be marked
	 */
	public static void onHotbarChange(Player player)
	{
		_hotbarUpdates.put(player.getName(), System.currentTimeMillis());
	}

	/**
	 * AviodAllies doesn't work. Leaving as a param as it sounds like something you may want in the future.
	 */
	public static Entity getEntityInSight(Player player, int rangeToScan, boolean avoidAllies, boolean avoidNonLiving,
										  boolean lineOfSight, float expandBoxesPercentage)
	{
		Location observerPos = player.getEyeLocation();
		Vector3D observerDir = new Vector3D(observerPos.getDirection());
		Vector3D observerStart = new Vector3D(observerPos);
		Vector3D observerEnd = observerStart.add(observerDir.multiply(rangeToScan));

		Entity hit = null;

		for (Entity entity : player.getNearbyEntities(rangeToScan, rangeToScan, rangeToScan))
		{
			if (entity == player || UtilPlayer.isSpectator(entity))
				continue;

			if (avoidNonLiving && !(entity instanceof LivingEntity))
				continue;

			double theirDist = player.getEyeLocation().distance(entity.getLocation());
			if (lineOfSight
					&& player.getLastTwoTargetBlocks(UtilBlock.blockAirFoliageSet, (int) Math.ceil(theirDist)).get(0)
					.getLocation().distance(player.getEyeLocation()) + 1 < theirDist)
				continue;

			Vector3D targetPos = new Vector3D(entity.getLocation());

			float width = (((CraftEntity) entity).getHandle().width / 1.8F) * expandBoxesPercentage;

			Vector3D minimum = targetPos.add(-width, -0.1 / expandBoxesPercentage, -width);
			Vector3D maximum = targetPos.add(width, ((CraftEntity) entity).getHandle().length * expandBoxesPercentage, width);

			if (hasIntersection(observerStart, observerEnd, minimum, maximum))
			{
				if (hit == null
						|| hit.getLocation().distanceSquared(observerPos) > entity.getLocation().distanceSquared(observerPos))
				{
					hit = entity;
				}
			}
		}
		return hit;
	}

	public static void message(Entity client, LinkedList<String> messageList)
	{
		UtilPlayerBase.message(client, messageList);
	}

	public static void message(Entity client, String message)
	{
		UtilPlayerBase.message(client, message);
	}

	public static void message(Entity client, LinkedList<String> messageList, boolean wiki)
	{
		UtilPlayerBase.message(client, messageList, wiki);
	}

	public static void message(Entity client, String message, boolean wiki)
	{
		UtilPlayerBase.message(client, message, wiki);
	}

	public static void messageSearchOnlineResult(Player caller, String player, int matchCount)
	{
		UtilPlayerBase.messageSearchOnlineResult(caller, player, matchCount);
	}

	public static Player searchExact(String name)
	{
		for (Player cur : UtilServer.getPlayers())
			if (cur.getName().equalsIgnoreCase(name))
				return cur;

		return null;
	}

	public static Player searchExact(UUID uuid)
	{
		return UtilServer.getServer().getPlayer(uuid);
	}


	public static void sendMatches(Player target, String module, Collection<String> matches)
	{
		sendMatches(target, module, matches, Function.identity());
	}

	public static <T> void sendMatches(Player target, String module, Collection<T> matches, Function<T, String> mapToString)
	{
		String matchString = matches.stream().map(mapToString).collect(Collectors.joining(C.mBody + ", " + C.mElem));
		UtilPlayer.message(target, F.main(module, "Matches [" + C.mElem + matchString + C.mBody + "]."));
	}

	public static String searchCollection(Player caller, String player, Collection<String> coll, String collName, boolean inform)
	{
		LinkedList<String> matchList = new LinkedList<String>();

		for (String cur : coll)
		{
			if (cur.equalsIgnoreCase(player))
				return cur;

			if (cur.toLowerCase().contains(player.toLowerCase()))
				matchList.add(cur);
		}

		// No / Non-Unique
		if (matchList.size() != 1)
		{
			if (!inform)
				return null;

			// Inform
			UtilPlayerBase.messageSearchOnlineResult(caller, player, 0);

			if (matchList.size() > 0)
			{
				StringBuilder matchString = new StringBuilder();
				for (String cur : matchList)
					matchString.append(cur).append(" ");

				message(caller,
						F.main(collName + " Search", "" + C.mBody + " Matches [" + C.mElem + matchString + C.mBody + "]."));
			}

			return null;
		}

		return matchList.get(0);
	}

	public static Player searchOnline(Player caller, String player, boolean inform)
	{
		return UtilPlayerBase.searchOnline(caller, player, inform);
	}

	public static void searchOffline(List<String> matches, final Callback<String> callback, final Player caller,
									 final String player, final boolean inform)
	{
		// No / Non-Unique
		if (matches.size() != 1)
		{
			if (!inform || !caller.isOnline())
			{
				callback.run(null);
				return;
			}

			// Inform
			message(caller,
					F.main("Offline Player Search", "" + C.mCount + matches.size() + C.mBody + " matches for [" + C.mElem
							+ player + C.mBody + "]."));

			if (matches.size() > 0)
			{
				String matchString = "";
				for (String cur : matches)
					matchString += cur + " ";
				if (matchString.length() > 1)
					matchString = matchString.substring(0, matchString.length() - 1);

				message(caller,
						F.main("Offline Player Search", "" + C.mBody + "Matches [" + C.mElem + matchString + C.mBody + "]."));
			}

			callback.run(null);
			return;
		}

		callback.run(matches.get(0));
	}

	public static LinkedList<Player> matchOnline(Player caller, String players, boolean inform)
	{
		LinkedList<Player> matchList = new LinkedList<Player>();

		String failList = "";

		for (String cur : players.split(","))
		{
			Player match = searchOnline(caller, cur, inform);

			if (match != null)
				matchList.add(match);

			else
				failList += cur + " ";
		}

		if (inform && failList.length() > 0)
		{
			failList = failList.substring(0, failList.length() - 1);
			message(caller, F.main("Online Player(s) Search", "" + C.mBody + "Invalid [" + C.mElem + failList + C.mBody + "]."));
		}

		return matchList;
	}

	public static List<Player> getNearby(Location loc, double maxDist)
	{
		return getNearby(loc, maxDist, false);
	}

	public static List<Player> getNearby(Location loc, double maxDist, boolean onlySurvival)
	{
		if (maxDist > 0)
		{
			maxDist *= maxDist;
		}
		LinkedList<Player> nearbyMap = new LinkedList<>();

		for (Player cur : loc.getWorld().getPlayers())
		{
			if (UtilPlayer.isSpectator(cur))
				continue;

			if (cur.isDead())
				continue;

			if (onlySurvival && cur.getGameMode() != GameMode.SURVIVAL)
				continue;

			double dist = UtilMath.offsetSquared(loc, cur.getLocation());
			if (dist > maxDist)
				continue;

			for (int i = 0; i < nearbyMap.size(); i++)
			{
				if (dist < UtilMath.offsetSquared(loc, nearbyMap.get(i).getLocation()))
				{
					nearbyMap.add(i, cur);
					break;
				}
			}

			if (!nearbyMap.contains(cur))
				nearbyMap.addLast(cur);
		}

		return nearbyMap;
	}

	public static Player getClosest(Location loc, Collection<Player> ignore)
	{
		return getClosest(loc, -1, ignore);
	}

	public static Player getClosest(Location loc, double maxDist, Collection<Player> ignore)
	{
		if (maxDist > 0)
		{
			maxDist *= maxDist;
		}
		Player best = null;
		double bestDist = 0;

		for (Player cur : loc.getWorld().getPlayers())
		{
			if (UtilPlayer.isSpectator(cur))
				continue;

			if (cur.isDead())
				continue;

			if (ignore != null && ignore.contains(cur))
				continue;

			double dist = UtilMath.offsetSquared(cur.getLocation(), loc);

			if (maxDist > 0 && dist > maxDist)
			{
				continue;
			}

			if (best == null || dist < bestDist)
			{
				best = cur;
				bestDist = dist;
			}
		}

		return best;
	}

	public static Player getClosest(Location loc, Entity... ignore)
	{
		return getClosest(loc, -1, ignore);
	}

	public static Player getClosest(Location loc, double maxDist, Entity... ignore)
	{
		if (maxDist > 0)
		{
			maxDist *= maxDist;
		}

		Player best = null;
		double bestDist = 0;

		for (Player cur : loc.getWorld().getPlayers())
		{
			if (UtilPlayer.isSpectator(cur))
				continue;

			if (cur.isDead())
				continue;

			//Ignore Check
			if (ignore != null)
			{
				boolean shouldIgnore = false;
				for (Entity ent : ignore)
				{
					if (cur.equals(ent))
					{
						shouldIgnore = true;
						break;
					}
				}

				if (shouldIgnore)
					continue;
			}

			double dist = UtilMath.offsetSquared(cur.getLocation(), loc);

			if (maxDist > 0 && dist > maxDist)
			{
				continue;
			}

			if (best == null || dist < bestDist)
			{
				best = cur;
				bestDist = dist;
			}
		}

		return best;
	}

	public static void kick(Player player, String module, String message)
	{
		kick(player, module, message, true);
	}

	public static void kick(Player player, String module, String message, boolean log)
	{
		if (player == null)
			return;

		String out = ChatColor.RED + module + ChatColor.WHITE + " - " + ChatColor.YELLOW + message;
		player.kickPlayer(out);

		// Log
		if (log)
			System.out.println("Kicked Client [" + player.getName() + "] for [" + module + " - " + message + "]");
	}

	public static void kick(Collection<Player> players, String module, String message, boolean log)
	{
		for (Player player : players)
		{
			kick(player, module, message, log);
		}
	}

	public static HashMap<Player, Double> getInRadius(Location loc, double dR)
	{
		HashMap<Player, Double> players = new HashMap<Player, Double>();

		for (Player cur : loc.getWorld().getPlayers())
		{
			if (UtilPlayer.isSpectator(cur))
				continue;

			double offset = UtilMath.offset(loc, cur.getLocation());

			if (offset < dR)
				players.put(cur, 1 - (offset / dR));
		}

		return players;
	}

	public static Map<Player, Double> getPlayersInPyramid(Player player, double angleLimit, double distance)
	{
		Map<Player, Double> players = new HashMap<>();

		for (Player cur : player.getWorld().getPlayers())
		{
			if (UtilPlayer.isSpectator(cur))
				continue;

			//Get lower offset (eye to eye, eye to feet)
			double offset = Math.min(UtilMath.offset(player.getEyeLocation(), cur.getEyeLocation()),
					UtilMath.offset(player.getEyeLocation(), cur.getLocation()));

			if (offset < distance && UtilAlg.isTargetInPlayerPyramid(player, cur, angleLimit))
				players.put(cur, 1 - (offset / distance));
		}

		return players;
	}

	public static void health(Player player, double mod)
	{
		if (player.isDead())
			return;

		double health = player.getHealth() + mod;

		if (health < 0)
			health = 0;

		if (health > player.getMaxHealth())
			health = player.getMaxHealth();

		player.setHealth(health);
	}

	public static void hunger(Player player, int mod)
	{
		if (player.isDead())
			return;

		int hunger = player.getFoodLevel() + mod;

		if (hunger < 0)
			hunger = 0;

		if (hunger > 20)
			hunger = 20;

		player.setFoodLevel(hunger);
	}

	public static boolean isOnline(String name)
	{
		return (searchExact(name) != null);
	}

	public static String safeNameLength(String name)
	{
		if (name.length() > 16)
			name = name.substring(0, 16);

		return name;
	}

	public static boolean isChargingBow(Player player)
	{
		if (!UtilGear.isMat(player.getItemInHand(), Material.BOW))
			return false;

		return (((CraftEntity) player).getHandle().getDataWatcher().getByte(0) & 1 << 4) != 0;
	}

	public static void clearInventory(Player player)
	{
		player.getInventory().clear();
		player.getInventory().setArmorContents(null);
	}

	public static void clearPotionEffects(Player player)
	{
		for (PotionEffect effect : player.getActivePotionEffects())
		{
			player.removePotionEffect(effect.getType());
		}
	}

	public static void sendPacket(Player player, Packet... packets)
	{
		PlayerConnection connection = ((CraftPlayer) player).getHandle().playerConnection;

		for (Packet packet : packets)
		{
			if (packet == null)
				continue;

			connection.sendPacket(packet);
		}
	}

	/**
	 * Get a random player within maxDist of the target location
	 * @param location The center location to look for the player
	 * @param maxDist The max distance from location that the player can be
	 * @return A random player that is within maxDist of location, or null if no players apply
	 */
	public static Player getRandomTarget(Location location, double maxDist)
	{
		List<Player> nearby = getNearby(location, maxDist, true);
		return nearby.size() > 0 ? nearby.get(RANDOM.nextInt(nearby.size())) : null;
	}

	public static boolean isSpectator(Entity player)
	{
		if (player instanceof Player)
			return ((CraftPlayer) player).getHandle().spectating;
		return false;
	}

	public static InventoryView swapToInventory(Player player, Inventory inv) {

		EntityPlayer nmsPlayer = ((CraftPlayer) player).getHandle();
		if (nmsPlayer.activeContainer != nmsPlayer.defaultContainer)
		{
			// Do this so that other inventories know their time is over.
			CraftEventFactory.handleInventoryCloseEvent(nmsPlayer);
			nmsPlayer.m();
		}
		return player.openInventory(inv);
	}

	/*
    public void setListName(Player player, CoreClient client)
    {
    	StringBuilder playerNameBuilder = new StringBuilder();

    	String prefixChar = "*";

    	if (client.NAC().IsUsing())							playerNameBuilder.append(ChatColor.GREEN + prefixChar);
    	else												playerNameBuilder.append(ChatColor.DARK_GRAY + prefixChar);

    	if (client.Rank().Has(Rank.OWNER, false))			playerNameBuilder.append(ChatColor.AQUA + prefixChar + ChatColor.RED);
    	else if (client.Rank().Has(Rank.MODERATOR, false))	playerNameBuilder.append(ChatColor.AQUA + prefixChar + ChatColor.GOLD);
    	else if (client.Rank().Has(Rank.DIAMOND, false))	playerNameBuilder.append(ChatColor.AQUA + prefixChar + ChatColor.WHITE);
    	else if (client.Rank().Has(Rank.EMERALD, false))	playerNameBuilder.append(ChatColor.GREEN + prefixChar + ChatColor.WHITE);
    	else if (client.Donor().HasDonated())				playerNameBuilder.append(ChatColor.YELLOW + prefixChar + ChatColor.WHITE);
    	else												playerNameBuilder.append(ChatColor.DARK_GRAY + prefixChar + ChatColor.WHITE);

    	playerNameBuilder.append(player.getName());

    	String playerName = playerNameBuilder.toString();

    	if (playerNameBuilder.length() > 16)
    	{
    		playerName = playerNameBuilder.substring(0, 16);
    	}

    	player.setPlayerListName(playerName);
    }
	 */

	public static Location getTargetLocation(Player player, double distance)
	{
		Vector looking = player.getLocation().getDirection().clone();
		looking.multiply(distance);
		return player.getEyeLocation().clone().add(looking);
	}

	public static Block getTarget(LivingEntity entity, HashSet<Byte> ignore, int maxDistance)
	{
		Iterator<Block> itr = new BlockIterator(entity, maxDistance);

		while (itr.hasNext())
		{
			Block block = itr.next();
			int id = block.getTypeId();

			if (ignore == null)
			{
				if (id != 0)
				{
					return block;
				}
			}
			else
			{
				if (!ignore.contains((byte)id))
				{
					return block;
				}
			}
		}

		return null;
	}

	public static boolean isGliding(Player player)
	{
		return ((CraftPlayer) player).getHandle().isGliding();
	}

	public static void setGliding(Player player, boolean gliding)
	{
		((CraftPlayer) player).getHandle().setGliding(gliding);
	}

	public static void setAutoDeploy(Player player, boolean autoDeploy)
	{
		((CraftPlayer) player).getHandle().setAutoWingsDeploy(autoDeploy);
	}

	public static void setGlidableWithoutWings(Player player, boolean glidableWithoutWings)
	{
		((CraftPlayer) player).getHandle().setGlidableWithoutWings(glidableWithoutWings);
	}

	public static void setAutoDeployDistance(Player player, float distance)
	{
		((CraftPlayer) player).getHandle().setWingsDeployAt(distance);
	}

	/**
	 * Sets the world border red screen for a player
	 * @param player
	 * @param warningDistance
	 */
	public static void sendRedScreen(Player player, int warningDistance)
	{
		WorldBorder worldBorder = WORLD_BORDERS.computeIfAbsent(player.getUniqueId(), uuid -> new WorldBorder());
		worldBorder.setCenter(player.getLocation().getX(), player.getLocation().getZ());
		worldBorder.setSize(10000);
		worldBorder.setWarningDistance(warningDistance);
		PacketPlayOutWorldBorder packet = new PacketPlayOutWorldBorder(worldBorder, PacketPlayOutWorldBorder.EnumWorldBorderAction.INITIALIZE);
		sendPacket(player, packet);
		WORLD_BORDERS.put(player.getUniqueId(), worldBorder);
	}

	/**
	 * Checks if player has a WorldBorder object stored
	 * @param player
	 * @return true if WorldBorder object is stored for that player
	 */
	public static boolean hasWorldBorder(Player player)
	{
		return WORLD_BORDERS.containsKey(player.getUniqueId());
	}

	/**
	 * Removes player from world border map
	 * @param player
	 */
	public static void removeWorldBorder(Player player)
	{
		if (hasWorldBorder(player))
		{
			sendRedScreen(player, 0);
			WORLD_BORDERS.remove(player.getUniqueId());
		}
	}

	public static MinecraftVersion getVersion(Player player)
	{
		int version = ((CraftPlayer) player).getHandle().playerConnection.networkManager.getVersion();
		
		return MinecraftVersion.fromInt(version);
	}

	/**
	 * Allows player to run specific command
	 * @param player The player to be allowed
	 * @param command The command that will be allowed
	 */
	public static void allowCommand(Player player, String command)
	{
		List<String> commandList = new ArrayList<>();
		if (ALLOWED_COMMANDS.containsKey(player.getUniqueId()))
			commandList = ALLOWED_COMMANDS.get(player.getUniqueId());
		if (!commandList.contains(command))
			commandList.add(command);
		ALLOWED_COMMANDS.put(player.getUniqueId(), commandList);
	}

	/**
	 * Disallows player to run specific command
	 * @param player The player to be disallowed
	 * @param command The command that will be disallowed
	 * @return True if player had command allowed
	 */
	public static boolean disallowCommand(Player player, String command)
	{
		if (!isCommandAllowed(player, command))
			return false;
		List<String> commandList = ALLOWED_COMMANDS.get(player.getUniqueId());
		commandList.remove(command);
		ALLOWED_COMMANDS.put(player.getUniqueId(), commandList);
		return true;
	}

	public static boolean isCommandAllowed(Player player, String command)
	{
		if (!ALLOWED_COMMANDS.containsKey(player.getUniqueId()))
			return false;
		if (!ALLOWED_COMMANDS.get(player.getUniqueId()).contains(command))
			return false;
		return true;
	}


	public static void removeAllowedCommands(Player player)
	{
		if (ALLOWED_COMMANDS.containsKey(player.getUniqueId()))
			ALLOWED_COMMANDS.remove(player.getUniqueId());
	}

	/**
	 * Check if the player is at least the specified amount of blocks in the air
	 * while provided block types are ignored.
	 *
	 * @see #isInAir(Player, int, Set)
	 */
	public static boolean isInAir(Player player, int minAir, Material... exclude)
	{
		EnumSet<Material> excludeSet = EnumSet.noneOf(Material.class);
		if (exclude != null) excludeSet.addAll(Arrays.asList(exclude));

		return isInAir(player, minAir, excludeSet);
	}

	/**
	 * Check if the player is at least the specified amount of blocks in the air
	 * while provided block types are ignored.
	 *
	 * @param player The Player to check
	 * @param minAir The min amount of Blocks to count as in the air
	 * @param exclude that are being ignored and count as Air
	 *
	 * @return if the Player is in the air
	 */
	public static boolean isInAir(Player player, int minAir, Set<Material> exclude)
	{
		Block block = player.getLocation().getBlock();
		int i = 0;
		while (i < minAir)
		{
			if (block.getType() != Material.AIR)
			{
				if (exclude.contains(block.getType()))
				{
					continue;
				}

				return false;
			}
			block = block.getRelative(BlockFace.DOWN);
			i++;
		}
		return true;
	}

	/**
	 * Sends a formatted clickable accept or deny (or view) message to a player
	 * Both ACCEPT and DENY will always be sent, but VIEW will only be sent when <code>viewCommand</code> is not null
	 *
	 * @param player The player to send the message to
	 * @param header The message header, such as Party or Game
	 * @param acceptCommand The command to be run if ACCEPT is clicked
	 * @param acceptDisplayText The text displayed when hovering over ACCEPT
	 * @param declineCommand The command to be run when DENY is clicked
	 * @param declineDisplayText The text displayed when hovering over DENY
	 * @param viewCommand <code>Optional</code> The command to be run when VIEW is clicked
	 * @param viewDisplayText <code>Optional</code> The text displayed when hovering over VIEW
	 */
	public static void sendAcceptOrDeny(Player player, String header,
										String acceptCommand, String acceptDisplayText,
										String declineCommand, String declineDisplayText,
										String viewCommand, String viewDisplayText)
	{
		TextComponent textComponent = new TextComponent(F.main(header, "Reply: "));

		TextComponent accept = new TextComponent("ACCEPT");
		accept.setColor(net.md_5.bungee.api.ChatColor.GREEN);
		accept.setBold(true);
		accept.setClickEvent(new ClickEvent(Action.RUN_COMMAND, acceptCommand));
		accept.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{
				new TextComponent(acceptDisplayText)
		}));

		textComponent.addExtra(accept);
		textComponent.addExtra(" ");

		TextComponent deny = new TextComponent("DENY");
		deny.setColor(net.md_5.bungee.api.ChatColor.RED);
		deny.setBold(true);
		deny.setClickEvent(new ClickEvent(Action.RUN_COMMAND, declineCommand));
		deny.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{
				new TextComponent(declineDisplayText)
		}));

		textComponent.addExtra(deny);

		if(viewCommand != null)
		{
			textComponent.addExtra(" ");

			TextComponent view = new TextComponent("VIEW");
			view.setColor(net.md_5.bungee.api.ChatColor.YELLOW);
			view.setBold(true);
			view.setClickEvent(new ClickEvent(Action.RUN_COMMAND, viewCommand));
			if(viewDisplayText != null)
			{
				view.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent[]{
						new TextComponent(viewDisplayText)
				}));
			}
			textComponent.addExtra(view);
		}

		player.spigot().sendMessage(textComponent);
	}

	public static void closeInventoryIfOpen(Player player)
	{
		if (hasOpenInventory(player))
			player.closeInventory();
	}

	public static boolean hasOpenInventory(Player player)
	{
		EntityPlayer entityPlayer = ((CraftPlayer) player).getHandle();
		return entityPlayer.activeContainer != entityPlayer.defaultContainer;
	}

	public static String getIp(Player player)
	{
		return player.getAddress().getAddress().getHostAddress();
	}

	/*
	 * Returns whether the UUID belongs to a slim skin
	 */
	public static boolean isSlimSkin(UUID playerUUID)
	{
		return (playerUUID.hashCode() & 1) == 1;
	}

	public static PlayerVersion getPlayerVersion(Player player)
	{
		int playerVersion = ((CraftPlayer) player).getHandle().playerConnection.networkManager.getVersion();
		if (playerVersion >= 210)
		{
			return PlayerVersion._1_10;
		}
		else if (playerVersion >= 107)
		{
			return PlayerVersion._1_9;
		}
		return PlayerVersion._1_8;
	}

	public enum PlayerVersion
	{
		_1_8(47, "1.8"),
		_1_9(107, "1.9"),
		_1_10(210, "1.10");

		private int _version;
		private String _friendlyName;

		PlayerVersion(int version, String friendlyName)
		{
			_version = version;
			_friendlyName = friendlyName;
		}

		public int getVersion()
		{
			return _version;
		}

		public String getFriendlyName()
		{
			return _friendlyName;
		}

		public boolean compare(PlayerVersion versionToCompare)
		{
			return versionToCompare.getVersion() >= getVersion();
		}
	}

	public static void playCustomSound(CustomSound sound)
	{
		if (sound == null)
		{
			return;
		}

		UtilServer.getPlayersCollection().forEach(player -> playCustomSound(player, sound));
	}

	public static void playCustomSound(Player player, CustomSound sound)
	{
		if (player == null || sound == null)
		{
			return;
		}

		Packet packet;
		int protocol = getProtocol(player);
		Location location = player.getLocation();

		if (protocol >= ProtocolVersion.v1_12)
		{
			packet = new PacketPlayOutCustomSoundEffect(sound.getAudioPath(), location.getX(), location.getY(), location.getZ(), 20, 1);
		}
		else
		{
			packet = new PacketPlayOutNamedSoundEffect(sound.getAudioPath(), location.getBlockX(), location.getBlockY(), location.getBlockZ(), 20, 1);
		}

		sendPacket(player, packet);
	}

	public interface CustomSound
	{

		String getAudioPath();

	}

	public static void teleportUniform(List<Player> players, List<Location> locations, BiConsumer<Player, Location> teleport)
	{
		// Find the number of spaces between each player,
		int spacesBetween = Math.max(Math.floorDiv(locations.size(), players.size()), 1);
		int spaceIndex = 0;

		// Teleport the players to locations every [spacesBetween] spaces
		for (Player player : players)
		{
			teleport.accept(player, locations.get(spaceIndex));

			spaceIndex = (spaceIndex + spacesBetween) % locations.size();
		}
	}

	public static void teleportUniform(List<Player> players, List<Location> locations)
	{
		teleportUniform(players, locations, Entity::teleport);
	}

	public static int getProtocol(Player player)
	{
		return ((CraftPlayer) player).getHandle().getProtocol();
	}

	/**
	 * Returns whether a given string is valid as a player's username.
	 * @param playerName - The name of the player
	 * @return Whether this name is synatactically valid. If this returns true it does not necessarily mean they exist.
	 */
	public static boolean isValidName(String playerName)
	{
		return USERNMAME_PATTERN.matcher(playerName).matches();
	}
}
