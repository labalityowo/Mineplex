package mineplex.mapparser.command;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.mapparser.GameType;
import mineplex.mapparser.MapData;
import mineplex.mapparser.MapParser;
import mineplex.mapparser.command.teleport.TeleportManager;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.WorldType;
import org.bukkit.entity.Player;

/**
 * Created by Shaun on 8/16/2014.
 */
public class CreateCommand extends BaseCommand
{
	private TeleportManager _teleportManager;

	public CreateCommand(TeleportManager teleportManager)
	{
		super(teleportManager.getPlugin(), "create");

		_teleportManager = teleportManager;
	}

	@Override
	public boolean execute(Player player, String alias, String[] args)
	{
		if (args.length < 1)
		{
			message(player, "Invalid Input. " + F.elem("/create <MapName> [-v]"));
			return true;
		}

		GameType gameType = GameType.InProgress;

		String worldName = "map/" + gameType.GetName() + "/" + args[0];

		if (getPlugin().doesMapExist(worldName))
		{
			message(player, "Map name is already in use!");
			return true;
		}

		boolean voidWorld = false;

		if (args.length == 2)
		{
			voidWorld = args[1].equalsIgnoreCase("-v");
		}


		WorldCreator worldCreator = new WorldCreator(worldName);
		worldCreator.environment(World.Environment.NORMAL);
		worldCreator.type(WorldType.FLAT);
		if (voidWorld)
		{
			//Cheeky little trick, saves time and energy.
			worldCreator.generatorSettings("3;minecraft:air;2");
			getPlugin().announce("Creating World: " + F.elem(worldName) + " -" + C.cRed + "VOID");
		}
		else
		{
			getPlugin().announce("Creating World: " + F.elem(worldName));
		}

		worldCreator.generateStructures(false);

		World world = Bukkit.getServer().createWorld(worldCreator);

		world.setSpawnLocation(0, 100, 0);

		message(player, "Teleporting to World: " + F.elem(worldName));

		_teleportManager.teleportPlayer(player, world.getSpawnLocation());

		//Give Access
		MapData mapData = getPlugin().getData(worldName);
		mapData.AdminList.add(player.getName());
		mapData.MapGameType = gameType;
		mapData.Write();

		return true;
	}
}
