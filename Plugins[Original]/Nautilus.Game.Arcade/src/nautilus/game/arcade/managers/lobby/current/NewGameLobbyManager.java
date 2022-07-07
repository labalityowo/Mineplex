package nautilus.game.arcade.managers.lobby.current;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import mineplex.core.common.timing.TimingManager;
import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilEnt;
import mineplex.core.game.kit.KitAvailability;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.deathtag.DeathTag;
import nautilus.game.arcade.game.games.hideseek.HideSeek;
import nautilus.game.arcade.game.games.smash.SuperSmash;
import nautilus.game.arcade.game.games.wither.WitherGame;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.kit.KitSorter;
import nautilus.game.arcade.kit.NullKit;
import nautilus.game.arcade.managers.LobbyEnt;
import nautilus.game.arcade.managers.lobby.LobbyManager;

public class NewGameLobbyManager extends LobbyManager
{

	public enum DataLoc
	{
		CARL,
		MISSIONS,
		AMP,
		SPAWN,;
	}

	private final File CONFIG = new File("world/WorldConfig.dat");

	private final Map<String, Location> _singleLocs = Maps.newHashMap();
	private final Map<String, List<Location>> _multipleLocs = Maps.newHashMap();
	private final boolean _run;

	public NewGameLobbyManager(ArcadeManager manager)
	{
		super(manager, null, null, null, null);
		_run = CONFIG.exists();

		if (_run)
		{
			try
			{
				readFile();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
	}

	@Override
	public void createTeams(Game game)
	{
		TimingManager.start("TeamGen");

		if (!_run)
		{
			System.out.println("Cannot generate teams, no WorldConfig.dat file was found, but a NEW.dat file was. If this is a testing server, feel free to ignore this message.");
			return;
		}

		List<GameTeam> list = game.GetTeamList().stream().filter(GameTeam::GetVisible).collect(Collectors.toList());
		List<Location> locations = _multipleLocs.get("TEAM");
		boolean doShift = true;

		if (locations == null)
		{
			locations = Lists.newArrayList();
			for (GameTeam team : game.GetTeamList())
			{
				String name = team.GetName().toUpperCase() + "_SHEEP";

				if (team.GetName().equalsIgnoreCase("Hiders"))
				{
					name = "RED_SHEEP";
				}
				if (team.GetName().equalsIgnoreCase("Hunters"))
				{
					name = "BLUE_SHEEP";
				}

				if (team.GetName().equalsIgnoreCase("Withers"))
				{
					name = "RED_SHEEP";
				}

				if (team.GetName().equalsIgnoreCase("Humans"))
				{
					name = "GREEN_SHEEP";
				}

				Location location = _singleLocs.get(name);

				if (location == null)
				{
					continue;
				}

				doShift = false;

				locations.add(location);
			}
		}

		if (locations.size() == 0)
		{
			return;
		}

		int shift = 0;

		if (doShift)
		{
			if (list.size() == 2)
			{
				shift = 4;
			}

			if (list.size() == 1)
			{
				shift = 6;
			}
		}

		if (game instanceof DeathTag && isGeneratePodiums())
		{
			int size = list.size();
			for (int i = 1; i < size; i++)
			{
				list.remove(i);
			}
		}

		if (game instanceof HideSeek)
		{
			Collections.reverse(list);
		}

		for (int i = 0; i < list.size(); i++)
		{
			GameTeam team = list.get(i);
			Location entLoc = locations.get(i).clone();

			if (isMPS())
			{
				entLoc.add(shift, 0, 0);
			}
			else
			{
				entLoc.add(0, 0, shift);
			}

			entLoc.setYaw(getYawToSpawn(entLoc, true));

			entLoc.getChunk().load();
			spawnTeamSheep(entLoc, team);
		}
		TimingManager.stop("TeamGen");
	}

	@Override
	public void createKits(Game game)
	{
		TimingManager.start("KitGen");

		if (!_run)
		{
			System.out.println("Cannot generate kits, no WorldConfig.dat file was found, but a NEW.dat file was. If this is a testing server, feel free to ignore this message.");
			return;
		}

		List<Kit> kitList = Lists.newArrayList(game.GetKits()).stream()
				.filter(kit -> !(kit instanceof NullKit))
				.filter(kit -> kit.GetAvailability() != KitAvailability.Hide)
				.collect(Collectors.toList());

		List<Location> locations = _multipleLocs.get("KIT");

		if (locations == null || locations.size() <= 1)
		{
			for (Kit kit : kitList)
			{
				String name = kit.GetName().toUpperCase().replace(" ", "_");
				Location entLoc = _singleLocs.get(name);
				setKit(kit, entLoc);
			}
		}
		else
		{
			int shift = 0;

			if (!(game instanceof DeathTag) && !(game instanceof WitherGame))
			{
				kitList.sort(new KitSorter());
			}

			if (kitList.size() == 3)
			{
				shift = 4;
			}

			if (kitList.size() == 1)
			{
				shift = 6;
			}

			if (kitList.size() == 2)
			{
				shift = 2;
			}

			if (kitList.size() == 4)
			{
				shift = 2;
			}

			if (isMPS())
			{
				Location blank = new Location(WORLD, -1, -1, -1);

				List<Location> buffer = Lists.newArrayList(locations);
				List<Location> removedPos = Lists.newArrayList();
				List<Location> removedNeg = Lists.newArrayList();

				for (int i = 0; i < locations.size(); i++)
				{
					Location location = locations.get(i);

					if (location.getBlockX() == -17)
					{
						buffer.set(i, blank);
						removedNeg.add(location);
					}

				}

				Collections.reverse(removedNeg);

				for (int i = 0; i < buffer.size(); i++)
				{
					Location locIn = buffer.get(i);

					if (!locIn.equals(blank))
					{
						continue;
					}

					if (!removedNeg.isEmpty())
					{
						Location locOut = removedNeg.remove(0);
						buffer.set(i, locOut);
					}
				}

				if (game instanceof SuperSmash)
				{
					for (int i = 0; i < locations.size(); i++)
					{
						Location location = locations.get(i);

						if (location.getBlockX() == 17)
						{
							buffer.set(i, blank);
							removedPos.add(location);
						}
					}

					Collections.reverse(removedPos);

					for (int i = 0; i < buffer.size(); i++)
					{
						Location locIn = buffer.get(i);

						if (!locIn.equals(blank))
						{
							continue;
						}

						if (!removedPos.isEmpty())
						{
							Location locOut = removedPos.remove(0);
							buffer.set(i, locOut);
						}
					}
				}

				removedNeg.clear();
				removedPos.clear();

				locations = buffer;
			}

			for (int i = 0; i < kitList.size(); i++)
			{
				Kit kit = kitList.get(i);
				int index = shift == 0 ? i : (locations.size() - 1) - i;

				if (isMPS())
				{
					index = i;
				}

				Location entLoc = locations.get(index);

				if (entLoc == null)
				{
					continue;
				}

				entLoc.clone().subtract(0, 0, shift);

				if (isGeneratePodiums())
				{
					byte data = 4;

					if (kit.GetAvailability() == KitAvailability.Gem)
					{
						data = 5;
					}
					else if (kit.GetAvailability() == KitAvailability.Achievement)
					{
						data = 2;
					}

					generatePodium(entLoc, data, getKitBlocks());
				}

				setKit(kit, entLoc);
			}
		}
		TimingManager.stop("KitGen");
	}

	private void spawnTeamSheep(Location entLoc, GameTeam team)
	{
		if (isGeneratePodiums())
		{
			generatePodium(entLoc, team.GetColorData(), getTeamBlocks());
		}

		Sheep ent = (Sheep) _manager.GetCreature().SpawnEntity(entLoc, EntityType.SHEEP);
		ent.setRemoveWhenFarAway(false);
		ent.setCustomNameVisible(true);

		ent.setColor(DyeColor.getByWoolData(team.GetColorData()));

		UtilEnt.vegetate(ent, true);
		UtilEnt.setFakeHead(ent, true);
		UtilEnt.ghost(ent, true, false);

		team.SetTeamEntity(ent);

		getTeams().put(ent, new LobbyEnt(ent, entLoc, team));
	}

	private void generatePodium(Location loc, byte data, Map<Block, Material> blocks)
	{
		Location location = loc.clone();
		int x = location.getBlockX();
		int y = location.getBlockY();
		int z = location.getBlockZ();

		//Steps
		for (int modX = x - 1; modX <= x + 1; modX++)
		{
			for (int modZ = z - 1; modZ <= z + 1; modZ++)
			{
				Location temp = new Location(location.getWorld(), modX, y, modZ);
				Block block = temp.getBlock();
				blocks.put(block, Material.AIR);
				MapUtil.QuickChangeBlockAt(temp, Material.STEP);
			}
		}

		//Wool
		for (int modX = x - 1; modX <= x + 1; modX++)
		{
			for (int modY = y - 1; modY < y; modY++)
			{
				for (int modZ = z - 1; modZ <= z + 1; modZ++)
				{
					Location temp = new Location(location.getWorld(), modX, modY, modZ);
					Block block = temp.getBlock();
					blocks.put(block, Material.AIR);
					MapUtil.QuickChangeBlockAt(temp, Material.WOOL, data);
				}
			}
		}
	}

	private void setKit(Kit kit, Location entLoc)
	{
		if (entLoc == null)
		{
			return;
		}

		entLoc.setYaw(getYawToSpawn(entLoc, true));
		kit.getGameKit().createNPC(entLoc);
	}

	public void readFile() throws IOException
	{
		FileInputStream fileStream = new FileInputStream(CONFIG);
		DataInputStream in = new DataInputStream(fileStream);
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		String line;
		String lastName = null;

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

			String name = tokens[0];

			if (name.equalsIgnoreCase("CUSTOM_LOCS"))
			{
				List<Location> locations = Lists.newArrayList();
				for (int i = 1; i < tokens.length; i++)
				{
					Location loc = StrToLoc(tokens[i]);

					if (loc == null || lastName == null)
					{
						continue;
					}

					if (!lastName.equalsIgnoreCase("SPAWN") || lastName.equalsIgnoreCase("CARL") || lastName.equalsIgnoreCase("MISSIONS"))
					{
						loc.subtract(0, 0.5, 0);
					}

					locations.add(loc);
				}

				if (locations.size() == 1)
				{
					_singleLocs.put(lastName, locations.get(0));
				}
				else
				{
					_multipleLocs.put(lastName, locations);
				}
			}
			else if (name.equalsIgnoreCase("MAP_NAME"))
			{
				if (tokens[1].toLowerCase().contains("halloween"))
				{
					WORLD.setTime(13850);
				}
			}
			else
			{
				lastName = tokens[1];
			}
		}

		setSpawn(_singleLocs.get(DataLoc.SPAWN.name()));

		Location carl = _singleLocs.get(DataLoc.CARL.name());
		if (carl != null)
		{
			carl.add(0, 0.5, 0);
			carl.setYaw(getYawToSpawn(carl, false));

			setCarl(carl);
		}

		Location missions = _singleLocs.get(DataLoc.MISSIONS.name());
		if (missions != null)
		{
			missions.add(0, 0.5, 0);
			missions.setYaw(getYawToSpawn(missions, false));

			setMissions(missions);
		}

		Location amp = _singleLocs.get(DataLoc.AMP.name());
		if (amp != null)
		{
			amp.setYaw(getYawToSpawn(amp, false));
			setAmpStand(amp);
		}
	}

	protected Location StrToLoc(String loc)
	{
		String[] coords = loc.split(",");

		try
		{
			return new Location(WORLD, Integer.valueOf(coords[0]) + 0.5, Integer.valueOf(coords[1]), Integer.valueOf(coords[2]) + 0.5);
		}
		catch (Exception e)
		{
			System.out.println("World Data Read Error: Invalid Location String [" + loc + "]");
		}

		return null;
	}

	public Map<String, List<Location>> getCustomLocs()
	{
		Map<String, List<Location>> ret = new HashMap<>();
		ret.putAll(_multipleLocs);
		for (Entry<String, Location> singleEntry : _singleLocs.entrySet())
		{
			ret.put(singleEntry.getKey(), Arrays.asList(singleEntry.getValue()));
		}
		return ret;
	}

	private float getYawToSpawn(Location location, boolean rounded)
	{
		float yaw = UtilAlg.GetYaw(UtilAlg.getTrajectory(location, getSpawn()));
		return rounded ? Math.round(yaw / 90) * 90 : yaw;
	}
}