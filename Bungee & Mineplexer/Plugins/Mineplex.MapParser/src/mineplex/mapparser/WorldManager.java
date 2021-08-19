package mineplex.mapparser;

import mineplex.core.common.util.ZipUtil;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

public class WorldManager
{
	private MapParser Host;
	
	public WorldManager(MapParser plugin)
	{
		Host = plugin;
	}
		
	public World prepMapParse(World world)
	{
		//Unload World
		Host.getServer().unloadWorld(world, true);
		
		//Delete Non-Map Files
		String[] folders = new File(world.getName()).list();
		for (String fileName : folders)
		{
			if (fileName.equalsIgnoreCase("level.dat"))
				continue;
			
			if (fileName.equalsIgnoreCase("region"))
				continue;
			
			if (fileName.equalsIgnoreCase("WorldConfig.dat"))
				continue;
			
			if (fileName.equalsIgnoreCase("Map.dat"))
				continue;

			FileUtils.deleteQuietly(new File(world.getName() + File.separator + fileName));
		}
		
		//Copy for Parsing

		String parseWorldName = "parse" + world.getName().replaceFirst("map", "");

		try 
		{
			//Delete if already exists
			File destination = new File(parseWorldName);
			if (destination.exists())
				FileUtils.deleteDirectory(destination);
			
			FileUtils.copyDirectory(new File(world.getName()), destination);
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
			return null;
		}

		return Bukkit.createWorld(new WorldCreator(parseWorldName));
	}

	public void finalizeParsedWorld(World world)
	{
		Host.getServer().unloadWorld(world, true);
		
		ArrayList<String> fileList = new ArrayList<String>();
		ArrayList<String> dirList = new ArrayList<String>();
		
		File[] files = new File(world.getName()).listFiles();
		for (File file : files)
		{
			if (file.getName().equalsIgnoreCase("level.dat"))
			{
				fileList.add(world.getName() + File.separator + file.getName());
				continue;
			}
			
			if (file.getName().equalsIgnoreCase("region"))
			{
				dirList.add(world.getName() + File.separator + file.getName());
				continue;
			}
			
			if (file.getName().equalsIgnoreCase("WorldConfig.dat"))
			{
				fileList.add(world.getName() + File.separator + file.getName());
				continue;
			}
			
			FileUtils.deleteQuietly(new File(world.getName() + File.separator + file.getName()));
		}
		
		MapData data = Host.getData(world.getName().replace("parse", "map"));
		GameType gameType = data.MapGameType;
		String fileName = gameType + "_" + data.MapName + ".zip";

		ZipUtil.ZipFolders(Paths.get(world.getName()).toAbsolutePath().toString(), fileName, dirList, fileList);
		
		try
		{
			File zipFile = new File(fileName);
			FileUtils.copyFile(zipFile, new File(File.separator + "home" + File.separator + "mineplex" + File.separator + "update" + File.separator + "maps" + File.separator + gameType.GetName() + File.separator + fileName));
			// Delete the zip file in root directory once zip is copied
			FileUtils.deleteQuietly(zipFile);
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		
		//Delete Parse Map
		FileUtils.deleteQuietly(new File(world.getName()));
	}
}
