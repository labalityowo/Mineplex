package mineplex.core.antihack.logging.builtin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.minecraft.server.v1_8_R3.MinecraftServer;

import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mineplex.anticheat.api.CheckDisabledEvent;
import com.mineplex.anticheat.api.PlayerViolationEvent;
import com.mineplex.anticheat.checks.Check;

import mineplex.core.antihack.logging.AnticheatMetadata;

import gnu.trove.map.TObjectIntMap;
import gnu.trove.map.TObjectLongMap;
import gnu.trove.map.hash.TObjectIntHashMap;
import gnu.trove.map.hash.TObjectLongHashMap;

public class ViolationInfoMetadata extends AnticheatMetadata
{
	private static final Location MUTABLE_LOCATION = new Location(null, 0, 0, 0);

	private static final String KEY_JOIN_TIME_MS = "join-time-ms";
	private static final String KEY_JOIN_TIME_TICK = "join-time-tick";

	private static final String KEY_CURRENT_TIME = "current-time";
	private static final String KEY_MS = "ms";
	private static final String KEY_TICK = "tick";

	private static final String KEY_VIOLATION_INFO = "violation-info";
	private static final String KEY_VL = "current-vl";
	private static final String KEY_MESSAGE = "msg";

	private static final String KEY_PLAYER_INFO = "player-info";
	private static final String KEY_LOCATION = "loc";
	private static final String KEY_WORLD = "world";
	private static final String KEY_X = "x";
	private static final String KEY_Y = "y";
	private static final String KEY_Z = "z";
	private static final String KEY_YAW = "yaw";
	private static final String KEY_PITCH = "pitch";
	private static final String KEY_PING = "ping";
	private static final String KEY_SERVER_TPS = "server-tps";

	private static final JsonObject VAL_CHECK_DISABLED;

	static
	{
		VAL_CHECK_DISABLED = new JsonObject();
		VAL_CHECK_DISABLED.addProperty(KEY_MESSAGE, "disabled");
	}

	private TObjectLongMap<UUID> _joinTime = new TObjectLongHashMap<>();
	private TObjectIntMap<UUID> _joinTimeTick = new TObjectIntHashMap<>();
	private Map<UUID, Map<Class<? extends Check>, List<JsonObject>>> _violations = new HashMap<>();

	@Override
	public String getId()
	{
		return "violation-info";
	}

	@Override
	public JsonElement build(UUID player)
	{
		JsonObject object = new JsonObject();
		object.addProperty(KEY_JOIN_TIME_MS, _joinTime.get(player));
		object.addProperty(KEY_JOIN_TIME_TICK, _joinTimeTick.get(player));
		_violations.get(player).forEach((check, list) ->
		{
			JsonArray checkElem = new JsonArray();
			list.forEach(checkElem::add);
			object.add(check.getName(), checkElem);
		});

		return object;
	}

	@Override
	public void remove(UUID player)
	{
		_joinTime.remove(player);
		_joinTimeTick.remove(player);
		_violations.remove(player);
	}

	@EventHandler
	public void onJoin(PlayerJoinEvent event)
	{
		long thisMs = System.currentTimeMillis();
		int thisTick = MinecraftServer.getServer().at();
		_joinTime.put(event.getPlayer().getUniqueId(), thisMs);
		_joinTimeTick.put(event.getPlayer().getUniqueId(), thisTick);
		_violations.put(event.getPlayer().getUniqueId(), new HashMap<>());
	}

	@EventHandler
	public void onDisabledCheck(CheckDisabledEvent event)
	{
		_violations.values().forEach(map ->
		{
			List<JsonObject> data = map.get(event.getCheck());
			if (data != null)
			{
				data.add(VAL_CHECK_DISABLED);
			}
		});
	}

	@EventHandler
	public void onViolation(PlayerViolationEvent event)
	{
		long thisMs = System.currentTimeMillis();
		int thisTick = MinecraftServer.getServer().at();

		List<JsonObject> violations = _violations.get(event.getPlayer().getUniqueId()).computeIfAbsent(event.getCheckClass(), key -> new ArrayList<>());

		JsonObject currentTime = new JsonObject();
		currentTime.addProperty(KEY_MS, thisMs);
		currentTime.addProperty(KEY_TICK, thisTick);

		JsonObject violationInfo = new JsonObject();
		violationInfo.addProperty(KEY_VL, event.getViolations());
		violationInfo.addProperty(KEY_MESSAGE, event.getMessage());

		event.getPlayer().getLocation(MUTABLE_LOCATION);

		JsonObject playerInfo = new JsonObject();
		JsonObject location = new JsonObject();
		location.addProperty(KEY_WORLD, MUTABLE_LOCATION.getWorld().getName());
		location.addProperty(KEY_X, MUTABLE_LOCATION.getX());
		location.addProperty(KEY_Y, MUTABLE_LOCATION.getY());
		location.addProperty(KEY_Z, MUTABLE_LOCATION.getZ());
		location.addProperty(KEY_YAW, MUTABLE_LOCATION.getYaw());
		location.addProperty(KEY_PITCH, MUTABLE_LOCATION.getPitch());

		playerInfo.add(KEY_LOCATION, location);
		playerInfo.addProperty(KEY_PING, Math.min(((CraftPlayer) event.getPlayer()).getHandle().ping, 1000));

		JsonObject data = new JsonObject();
		data.add(KEY_CURRENT_TIME, currentTime);
		data.add(KEY_VIOLATION_INFO, violationInfo);
		data.add(KEY_PLAYER_INFO, playerInfo);
		data.addProperty(KEY_SERVER_TPS, MinecraftServer.getServer().recentTps[0]);

		violations.add(data);
	}
}