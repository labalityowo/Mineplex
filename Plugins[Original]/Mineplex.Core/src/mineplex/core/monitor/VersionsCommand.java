package mineplex.core.monitor;

import java.lang.reflect.Field;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;

import com.mineplex.ProtocolVersion;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;

/**
 * Statistics on versions
 * @author Dan
 */
public class VersionsCommand extends CommandBase<LagMeter>
{
	private static Map<Integer, String> PRETTY_VERSIONS;

	public VersionsCommand(LagMeter plugin)
	{
		super(plugin, LagMeter.Perm.VERSIONS_COMMAND, "versions", "getver");
	}

	private void ensureVersions()
	{
		if (PRETTY_VERSIONS == null)
		{
			PRETTY_VERSIONS = new HashMap<>();
			for (Field field : ProtocolVersion.class.getFields())
			{
				try
				{
					int protocol = field.getInt(null);
					String version = field.getName().replace("v", "").replace("_", ".");
					version += " (" + protocol + ")";

					PRETTY_VERSIONS.put(protocol, version);
				} catch (ReflectiveOperationException ex) { }
			}
		}
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		ensureVersions();

		if (args.length == 0)
		{
			Map<Integer, Integer> versions = new HashMap<>();
			for (Player player : Bukkit.getOnlinePlayers())
			{
				int version = ((CraftPlayer) player).getHandle().getProtocol();
				int players = versions.getOrDefault(version, 0);
				versions.put(version, players + 1);
			}

			UtilPlayer.message(caller, F.main("Version", "Distribution on " + C.cGold
			                                             + UtilServer.getServerName()));

			List<Map.Entry<Integer, Integer>> sorted = versions
					.entrySet().stream()
					.sorted(Comparator.comparing(Map.Entry::getValue, (i1, i2) -> -i1.compareTo(i2)))
					.collect(Collectors.toList());
			for (Map.Entry<Integer, Integer> entry : sorted)
			{
				int protocol = entry.getKey();
				String pretty = PRETTY_VERSIONS.computeIfAbsent(protocol, x -> Integer.toString(protocol));

				UtilPlayer.message(caller,
						F.main("Version", C.cYellow + pretty + C.cGray + ": " + C.cGreen
						                  + entry.getValue() + C.cGray + " players"));
			}
		}
		else if (args.length == 1)
		{
			List<Player> players = UtilPlayer.matchOnline(caller, args[0], true);
			if (!players.isEmpty())
			{
				Player player = players.get(0);
				int protocol = ((CraftPlayer) player).getHandle().getProtocol();
				String pretty = PRETTY_VERSIONS.computeIfAbsent(protocol, x -> Integer.toString(protocol));

				UtilPlayer.message(caller,
						F.main("Version", C.cYellow + player.getName() + C.cGray + " is on version "
						                  + C.cGreen + pretty));
			}
		}
		else
		{
			UtilPlayer.message(caller, F.main("Version", "Invalid argument list."));
		}
	}
}