package mineplex.mapparser.command;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayerBase;
import mineplex.mapparser.GameType;
import mineplex.mapparser.MapData;
import mineplex.mapparser.MapParser;

/**
 * Created by Shaun on 8/15/2014.
 */
public class ListCommand extends BaseCommand
{
	//private static final List<String> AUTHOR_ALISES = Arrays.asList("author", "creator", "a");
	private static final List<String> NAME_ALIASES = Arrays.asList("name", "mapname", "title", "n");
	private static final List<String> TYPE_ALIASES = Arrays.asList("type", "game", "gametype", "g");

	public ListCommand(MapParser plugin)
	{
		super(plugin, "list");

		setUsage("/list <author|name|type> <search term(s)>");
	}

	@Override
	public boolean execute(Player player, String alias, String[] args)
	{
		if (args.length == 0)
		{
			UtilPlayerBase.message(player, F.main("Parser", "Listing Maps;"));

			boolean colorSwitch = false;
			
			for (GameType gameType : GameType.values())
			{
				if (gameType == GameType.InProgress)
				{
					continue;
				}

				if (listMapsForGameType(player, gameType, colorSwitch))
				{
					colorSwitch = !colorSwitch;
				}
			}
		}
		else if (args.length == 1)
		{
			message(player, F.elem(getUsage()));
		}
		else
		{
			String subCommand = args[0].toLowerCase();

			if (TYPE_ALIASES.contains(subCommand))
			{
				String input = args[1];

				GameType gameType = getGameType(input);

				if (gameType == null)
				{
					getPlugin().sendValidGameTypes(player);
					return true;
				}

				UtilPlayerBase.message(player, F.main("Parser", "Listing Maps for gametype " + F.elem(gameType.GetName())));
				listMapsForGameType(player, gameType, false);
			}
			else
			{
				String search = Arrays.asList(args).subList(1, args.length).stream().collect(Collectors.joining());

				if (NAME_ALIASES.contains(subCommand))
				{
					List<String> mapNames = GameType.getAllMapNames()
							.stream()
							.filter(n -> n.toLowerCase().contains(search))
							.collect(Collectors.toList());

					message(player, "Listing maps whose name contains " + F.elem(search) + C.mBody + ":");
					message(player, getMapsMessage(mapNames, false));
				}
				else
				{
					message(player, "Invalid sub-command.");
				}
			}
		}

		return true;
	}

	private GameType getGameType(String input)
	{
		GameType gameType;
		if (input.equalsIgnoreCase("p"))
		{
			gameType = GameType.InProgress;
		}
		else
		{
			try
			{
				gameType = GameType.valueOf(input);
			}
			catch (Exception e)
			{
				return null;
			}
		}

		return gameType;
	}

	private String getMapsMessage(List<String> mapNames, boolean colorSwitch)
	{
		StringBuilder maps = new StringBuilder();
		ChatColor color = ChatColor.AQUA;
		if (colorSwitch)
			color = ChatColor.GREEN;

		for (String name : mapNames)
		{
			maps.append(color).append(name).append(" ");

			if (color == ChatColor.AQUA)
				color = ChatColor.DARK_AQUA;
			else if (color == ChatColor.DARK_AQUA)
				color = ChatColor.AQUA;
			else if (color == ChatColor.GREEN)
				color = ChatColor.DARK_GREEN;
			else color = ChatColor.GREEN;
		}

		return maps.toString();
	}

	private boolean listMapsForGameType(Player player, GameType gameType, boolean colorSwitch)
	{
			List<String> mapNames = gameType.getMapNames();

			if (mapNames == null)
			{
				return false;
			}

			String maps = getMapsMessage(mapNames, colorSwitch);

			// Print line of maps for specific gametype
			if (maps.length() > 0)
			{
				player.sendMessage(F.elem(ChatColor.RESET + C.Scramble + "!" + ChatColor.RESET + C.Bold + gameType.name()) + "> " + maps);
				
				return true;
			}
			
			return false;
	}

	public List<MapData> getAllMapData()
	{
		List<String> mapNames = GameType.getAllMapNames();
		List<MapData> mapData = new ArrayList<>();

		for (String mapName : mapNames)
		{
			MapData data = getPlugin().getData(mapName);

			if (data == null)
			{
				continue;
			}

			mapData.add(data);
		}

		return mapData;
	}
}
