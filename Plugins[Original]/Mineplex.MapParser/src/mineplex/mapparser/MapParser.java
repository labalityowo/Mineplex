package mineplex.mapparser;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilBlockBase;
import mineplex.core.common.util.UtilPlayerBase;
import mineplex.mapparser.command.AddLoreCommand;
import mineplex.mapparser.command.AddSplashTextCommand;
import mineplex.mapparser.command.AdminCommand;
import mineplex.mapparser.command.AuthorCommand;
import mineplex.mapparser.command.ClearLoreCommand;
import mineplex.mapparser.command.CommandListCommand;
import mineplex.mapparser.command.CopyCommand;
import mineplex.mapparser.command.CopySchematicsCommand;
import mineplex.mapparser.command.CreateCommand;
import mineplex.mapparser.command.CurrentlyLiveCommand;
import mineplex.mapparser.command.DeleteCommand;
import mineplex.mapparser.command.FlySpeedCommand;
import mineplex.mapparser.command.GameTypeCommand;
import mineplex.mapparser.command.HubCommand;
import mineplex.mapparser.command.GameTypeInfoCommand;
import mineplex.mapparser.command.ItemNameCommand;
import mineplex.mapparser.command.ListCommand;
import mineplex.mapparser.command.LockCommand;
import mineplex.mapparser.command.MapCommand;
import mineplex.mapparser.command.MapInfoCommand;
import mineplex.mapparser.command.NameCommand;
import mineplex.mapparser.command.PMCommand;
import mineplex.mapparser.command.ParseCommand;
import mineplex.mapparser.command.ParseCommand200;
import mineplex.mapparser.command.ParseCommand400;
import mineplex.mapparser.command.ParseCommand600;
import mineplex.mapparser.command.ParseCommand1000;
import mineplex.mapparser.command.PlayerHeadCommand;
import mineplex.mapparser.command.RefreshWorldEditCommand;
import mineplex.mapparser.command.RenameCommand;
import mineplex.mapparser.command.SaveCommand;
import mineplex.mapparser.command.SetSpawnCommand;
import mineplex.mapparser.command.SpawnCommand;
import mineplex.mapparser.command.TimeCommand;
import mineplex.mapparser.command.WarpCommand;
import mineplex.mapparser.command.WorldsCommand;
import mineplex.mapparser.command.teleport.BackCommand;
import mineplex.mapparser.command.teleport.TeleportCommand;
import mineplex.mapparser.command.teleport.TeleportManager;
import mineplex.mapparser.command.teleport.TopCommand;
import mineplex.mapparser.module.Module;
import mineplex.mapparser.module.modules.CommandModule;
import mineplex.mapparser.module.modules.EventModule;
import mineplex.mapparser.module.modules.MMMazeModule;
import mineplex.mapparser.module.modules.SignModule;
import mineplex.mapparser.module.modules.TreeToolModule;

public class MapParser extends JavaPlugin
{
	private WorldManager _worldManager;

	private Parse _curParse = null;
	private final Map<Class<? extends Module>, Module> _modules = Maps.newHashMap();
	private final Map<GameType, GameTypeInfo> _infoMap = Maps.newHashMap();
	private final Map<String, MapData> _mapData = Maps.newHashMap();
	public final Set<String> _mapsBeingZipped = Sets.newHashSet();
	private List<String> _additionalText = Lists.newArrayList();
	private Location _spawnLocation;
	private TeleportManager _teleportManager;

	@Override
	public void onEnable()
	{
		_worldManager = new WorldManager(this);

		getServer().getWorlds().get(0).setSpawnLocation(0, 106, 0);
		_spawnLocation = new Location(getServer().getWorlds().get(0), 0, 106, 0);

		//Updates
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new Ticker(this), 1, 1);

		_teleportManager = new TeleportManager(this);

		new EventModule(this);
		new MMMazeModule(this);
		new SignModule(this);
		new TreeToolModule(this);

		CommandModule commandModule = new CommandModule(this);

		// Normal Commands
		commandModule.add(new AuthorCommand(this));
		commandModule.add(new AdminCommand(this));
		commandModule.add(new CopySchematicsCommand(this));
		commandModule.add(new DeleteCommand(this));
		commandModule.add(new GameTypeCommand(this));
		commandModule.add(new ListCommand(this));
		commandModule.add(new NameCommand(this));
		commandModule.add(new ParseCommand200(this));
		commandModule.add(new ParseCommand400(this));
		commandModule.add(new ParseCommand600(this));
		commandModule.add(new ParseCommand1000(this));
		commandModule.add(new RenameCommand(this));
		commandModule.add(new SaveCommand(this));
		commandModule.add(new WorldsCommand(this));
		commandModule.add(new CopyCommand(this));
		commandModule.add(new SetSpawnCommand(this));
		commandModule.add(new ItemNameCommand(this));
		commandModule.add(new AddLoreCommand(this));
		commandModule.add(new ClearLoreCommand(this));
		commandModule.add(new GameTypeInfoCommand(this));
		commandModule.add(new LockCommand(this));
		commandModule.add(new PlayerHeadCommand(this));
		commandModule.add(new RefreshWorldEditCommand(this));
		commandModule.add(new WarpCommand(this));
		commandModule.add(new CurrentlyLiveCommand(this));
		commandModule.add(new AddSplashTextCommand(this));
		commandModule.add(new PMCommand(this));
		commandModule.add(new TimeCommand(this));
		commandModule.add(new MapInfoCommand(this));
		commandModule.add(new ParseCommand(this));
		commandModule.add(new FlySpeedCommand(this));
		commandModule.add(new CommandListCommand(this, commandModule));

		// Teleport-related commands
		commandModule.add(new HubCommand(_teleportManager));
		commandModule.add(new CreateCommand(_teleportManager));
		commandModule.add(new MapCommand(_teleportManager));

		commandModule.add(new TeleportCommand(_teleportManager));
		commandModule.add(new BackCommand(_teleportManager));
		commandModule.add(new TopCommand(_teleportManager));

		commandModule.add(new SpawnCommand(_teleportManager));

		loadInfo();
		addSplashText();
	}

	@Override
	public void onDisable()
	{
		_infoMap.values().forEach(this::saveInfo);
		saveSplashText();
	}

	public Parse getCurParse()
	{
		return _curParse;
	}

	public void sendValidGameTypes(Player player)
	{
		UtilPlayerBase.message(player, F.main("Parser", "Valid Game Types;"));

		String gameTypes = "";

		for (GameType game : GameType.values())
		{
			gameTypes += game.toString() + " ";
		}

		player.sendMessage(gameTypes);
	}

	public GameTypeInfo getInfo(GameType type)
	{
		return _infoMap.get(type);
	}

	private void addSplashText()
	{
		File file = new File(getDataFolder(), "join-text.yml");
		if(!file.exists())
		{
			try
			{
				file.createNewFile();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		List<String> messages = config.getStringList("messages");
		if(messages == null)
		{
			messages = Lists.newArrayList();
		}
		_additionalText = messages;
	}

	private void saveSplashText()
	{
		File file = new File(getDataFolder(), "join-text.yml");
		if(!file.exists())
		{
			try
			{
				file.createNewFile();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		config.set("messages", _additionalText);
		try
		{
			config.save(file);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private void loadInfo()
	{
		File file = new File(getDataFolder(), "info.yml");
		if(!file.exists())
		{
			try
			{
				file.createNewFile();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);

		for(String s : config.getKeys(false))
		{
			GameType gameType = GameType.valueOf(s);
			List<String> messages = config.getStringList(s);
			_infoMap.put(gameType, new GameTypeInfo(gameType, messages));
		}
	}

	private void saveInfo(GameTypeInfo info)
	{
		File file = new File(getDataFolder(), "info.yml");
		if(!file.exists())
		{
			try
			{
				file.createNewFile();
			} catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		FileConfiguration config = YamlConfiguration.loadConfiguration(file);
		config.set(info.getGameType().name(), info.getInfo());
		try
		{
			config.save(file);
		} catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	public void announce(String msg)
	{
		for (Player player : Bukkit.getOnlinePlayers())
		{
			player.sendMessage(C.cGold + msg);
		}

		System.out.println("[Announce] " + msg);
	}

	public boolean doesMapExist(String mapName, GameType gameType)
	{
		return doesMapExist(getWorldString(mapName, gameType));
	}

	public boolean doesMapExist(String worldName)
	{
		File file = new File(worldName);

		return file.exists() && file.isDirectory();

	}

	public String getShortWorldName(String worldName)
	{
		int lastIndexOfSeperator = worldName.lastIndexOf('/');

		if (lastIndexOfSeperator != -1)
			return worldName.substring(lastIndexOfSeperator + 1);

		return worldName;
	}

	public World getMapWorld(String worldName)
	{
		for (World world : this.getServer().getWorlds())
		{
			if (world.getName().equals(worldName))
				return world;
		}

		return null;
	}

	public String getWorldString(String mapName, GameType type)
	{
		return "map" + "/" + type.GetName() + "/" + mapName;
	}

	public List<String> getMapsByName(String name)
	{
		name = name.toLowerCase();

		List<String> maps = new LinkedList<>();
		boolean matchesExact = false;

		for (GameType type : GameType.values())
		{

			File mapsFolder = new File("map" + File.separator + type.GetName());
			if (!mapsFolder.exists() || mapsFolder.listFiles() == null)
				continue;

			for (File file : mapsFolder.listFiles())
			{
				if (!file.isDirectory())
					continue;

				if (!file.getName().toLowerCase().contains(name))
					continue;

				if (file.getName().equalsIgnoreCase(name))
					matchesExact = true;

				maps.add(getWorldString(file.getName(), type));
			}
		}

		if (matchesExact)
		{
			Iterator<String> it = maps.iterator();
			while (it.hasNext())
			{
				String mapString = it.next();

				if (!mapString.toLowerCase().endsWith(name))
				{
					it.remove();
				}
			}
		}

		return maps;
	}

	public MapData getData(String mapName)
	{
		if (_mapData.containsKey(mapName))
			return _mapData.get(mapName);

		MapData data = new MapData(mapName);

		_mapData.put(mapName, data);

		return data;
	}

	public Location getSpawnLocation()
	{
		return _spawnLocation;
	}

	public WorldManager getWorldManager()
	{
		return _worldManager;
	}

	public void setCurrentParse(Parse parse)
	{
		_curParse = parse;
	}

	public Set<String> getMapsBeingZipped()
	{
		return _mapsBeingZipped;
	}
	
	public Set<Block> searchLog(Set<Block> blocks, Block current)
	{
		//Not Tree
		if (current.getType() != Material.LOG && current.getType() != Material.LEAVES)
			return blocks;

		if (!blocks.add(current))
			return blocks;

		for (Block other : UtilBlockBase.getSurrounding(current, true))
		{
			if (current.getType() != Material.LOG && current.getType() != Material.LEAVES)
				continue;

			if (blocks.contains(other))
				continue;

			//Dont spread from leaves to log
			if (current.getType() == Material.LEAVES && other.getType() == Material.LOG)
				continue;

			searchLog(blocks, other);
		}

		return blocks;
	}

	public Map<Class<? extends Module>, Module> getModules()
	{
		return _modules;
	}

	public void setInfo(GameType gameType, GameTypeInfo info)
	{
		_infoMap.put(gameType, info);
	}

	public List<String> getAdditionalText()
	{
		return _additionalText;
	}

	public void addAdditionalText(String s)
	{
		_additionalText.add(s);
	}

	public World getWorldFromName(String worldName)
	{
		World world = getMapWorld(worldName);
		if (world == null)
		{
			if (doesMapExist(worldName))
			{
				world = Bukkit.createWorld(new WorldCreator(worldName));
			}
			else
			{
				return  null;
			}
		}

		return world;
	}
}
