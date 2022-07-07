package mineplex.mapparser;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayerBase;
import mineplex.core.common.util.UtilWorld;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class MapData
{
	public String MapFolder;
	public boolean _currentlyLive;
	public boolean _locked;
	public Map<String, Location> _warps;

	public GameType MapGameType = null;
	public String MapName = "null";
	public String MapCreator = "null";

	public Set<String> AdminList;

	public MapData(String mapFolder)
	{
		MapFolder = mapFolder;

		AdminList = Sets.newHashSet();
		_warps = Maps.newHashMap();
		_currentlyLive = false;

		if ((new File(MapFolder + File.separator + "Map.dat")).exists())
		{
			Read();
		} else
		{
			Write();
		}
	}

	public void Read()
	{
		String line = null;

		try
		{
			FileInputStream fstream = new FileInputStream(MapFolder + File.separator + "Map.dat");
			DataInputStream in = new DataInputStream(fstream);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));

			while ((line = br.readLine()) != null)
			{
				String[] tokens = line.split(":");

				if (tokens.length < 2)
				{
					continue;
				}

				if (tokens[0].length() == 0)
				{
					continue;
				}

				if(tokens[0].equalsIgnoreCase("LOCKED"))
				{
					_locked = tokens[1].equalsIgnoreCase("true");
					continue;
				}

				if (tokens[0].equalsIgnoreCase("currentlyLive"))
				{
					_currentlyLive = tokens[1].equalsIgnoreCase("true");
					continue;
				}

				if (tokens[0].equalsIgnoreCase("warps"))
				{
					for (String s : tokens[1].split(";"))
					{
						String[] str = s.split("@");
						_warps.put(str[0], UtilWorld.strToLoc(str[1]));
					}
					continue;
				}

				//Name & Author
				if (tokens[0].equalsIgnoreCase("MAP_NAME"))
				{
					MapName = tokens[1];
					continue;
				}

				if (tokens[0].equalsIgnoreCase("MAP_AUTHOR"))
				{
					MapCreator = tokens[1];
					continue;
				}

				if (tokens[0].equalsIgnoreCase("GAME_TYPE"))
				{
					try
					{
						MapGameType = GameType.valueOf(tokens[1] == null ? "Unknown" : tokens[1]);
					} catch (Exception e)
					{
						MapGameType = GameType.Unknown;
					}
					continue;
				}
				if (tokens[0].equalsIgnoreCase("ADMIN_LIST") || tokens[0].equalsIgnoreCase("BUILD_LIST"))
				{
					Collections.addAll(AdminList, tokens[1].split(","));
				}
			}

			in.close();
		} catch (Exception e)
		{
			e.printStackTrace();
			System.err.println("Line: " + line);
		}
	}

	public void Write()
	{
		//Save
		try
		{
			FileWriter fstream = new FileWriter(MapFolder + File.separator + "Map.dat");
			BufferedWriter out = new BufferedWriter(fstream);

			out.write("MAP_NAME:" + MapName);
			out.write("\n");
			out.write("MAP_AUTHOR:" + MapCreator);
			out.write("\n");
			out.write("GAME_TYPE:" + MapGameType);

			String adminList = "";

			for (String cur : AdminList)
			{
				adminList += cur + ",";
			}

			out.write("\n");
			out.write("ADMIN_LIST:" + adminList);
			out.write("\n");
			out.write("currentlyLive:" + _currentlyLive);
			out.write("\n");
			out.write("warps:" + warpsToString());
			out.write("\n");
			out.write("LOCKED:" + _locked);

			out.close();
		} catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	public String warpsToString()
	{
		StringBuilder builder = new StringBuilder();
		int i = 0;
		for (Entry<String, Location> entry : _warps.entrySet())
		{
			builder.append(entry.getKey()).append("@").append(UtilWorld.locToStr(entry.getValue()));
			if (++i != _warps.size())
			{
				builder.append(",");
			}
		}
		return builder.toString();
	}

	public boolean HasAccess(Player player)
	{
		return AdminList.contains(player.getName()) || player.isOp();
	}

	public boolean CanJoin(Player player)
	{
		return !_locked || (player.isOp() || AdminList.contains(player.getName()));
	}

	public boolean CanRename(Player player)
	{
		return !_locked || (player.isOp() || AdminList.contains(player.getName()));
	}

	public void sendInfo(Player player)
	{
		UtilPlayerBase.message(player, F.value("Map Name", MapName));
		UtilPlayerBase.message(player, F.value("Author", MapCreator));
		UtilPlayerBase.message(player, F.value("Game Type", MapGameType.GetName()));
	}
}
