package mineplex.game.nano.world;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.bukkit.Difficulty;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import mineplex.core.common.util.FileUtil;
import mineplex.core.common.util.MapUtil;
import mineplex.core.common.util.WorldUtil;
import mineplex.core.common.util.ZipUtil;
import mineplex.core.common.util.worldgen.WorldGenCleanRoom;
import mineplex.core.world.MineplexWorld;
import mineplex.game.nano.NanoManager;
import mineplex.game.nano.game.Game;

public class GameWorld
{

	private static final String DIRECTORY_PREFIX = "Game";
	private static final File GAME_ID_FILE = new File("GameId.dat");

	public static void deleteOldFolders(NanoManager manager)
	{
		File[] files = new File(".").listFiles((dir, name) -> name.startsWith(DIRECTORY_PREFIX));

		if (files == null)
		{
			return;
		}

		for (File file : files)
		{
			if (!file.isDirectory())
			{
				continue;
			}

			FileUtil.DeleteFolder(file);
			manager.log("Deleted Old Game: " + file.getName());
		}
	}

	public static int getNextId() throws IOException, NumberFormatException
	{
		int write = 0;

		if (GAME_ID_FILE.exists())
		{
			write = Integer.parseInt(Files.readAllLines(GAME_ID_FILE.toPath()).get(0));
		}

		BufferedWriter writer = Files.newBufferedWriter(GAME_ID_FILE.toPath());
		writer.write(String.valueOf(write + 1));
		writer.close();

		return write;
	}

	private final Game _game;
	private final File _mapZip;
	private final int _id;

	private World _world;

	public GameWorld(Game game, File mapZip)
	{
		_game = game;
		_mapZip = mapZip;

		int id = 0;

		try
		{
			id = getNextId();
		}
		catch (Exception e)
		{
			GAME_ID_FILE.delete();
			e.printStackTrace();
		}

		_id = id;
		game.getManager().log("Game Id: " + id);
	}

	public void loadWorld()
	{
		String mapName = _mapZip.getName();
		int dotIndex = mapName.lastIndexOf('.');
		mapName = dotIndex == -1 ? mapName : mapName.substring(0, dotIndex);
		String directory = DIRECTORY_PREFIX + _id + "_" + _game.getGameType().getName() + "_" + mapName;

		_game.getManager().runAsync(() ->
		{
			new File(directory + File.separator + "region").mkdirs();
			new File(directory + File.separator + "data").mkdirs();

			ZipUtil.UnzipToDirectory(_mapZip.getAbsolutePath(), directory);

			_game.getManager().runSync(() ->
			{
				WorldCreator creator = new WorldCreator(directory);
				creator.generator(new WorldGenCleanRoom());
				_world = WorldUtil.LoadWorld(creator);

				if (_world == null)
				{
					return;
				}

				_world.setDifficulty(Difficulty.HARD);
				_world.setTime(6000);
				_world.setGameRuleValue("showDeathMessages", "false");
				_world.setGameRuleValue("doDaylightCycle", "false");
				_world.setGameRuleValue("keepInventory", "true");

				_game.getManager().runAsync(() ->
				{
					MineplexWorld mineplexWorld = new MineplexWorld(_world);

					_game.getManager().runSync(() -> _game.setupMineplexWorld(mineplexWorld));
				});
			});
		});
	}

	public void unloadWorld()
	{
		if (_world == null)
		{
			return;
		}

		MapUtil.UnloadWorld(_game.getManager().getPlugin(), _world);
		MapUtil.ClearWorldReferences(_world.getName());
		FileUtil.DeleteFolder(new File(_world.getName()));

		_world = null;
	}
}
