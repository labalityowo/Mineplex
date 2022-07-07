package mineplex.mapparser.command;

import org.bukkit.World;
import org.bukkit.entity.Player;

import mineplex.mapparser.MapParser;

public abstract class MapAdminCommand extends BaseCommand
{
	public MapAdminCommand(MapParser plugin, String... aliases)
	{
		super(plugin, aliases);
	}

	@Override
	public boolean canRun(Player player)
	{
		if (player.isOp())
		{
			return true;
		}

		World world = player.getWorld();

		if (world.getName().equals("world_lobby"))
		{
			return false;
		}

		return getPlugin().getData(world.getName()).HasAccess(player);
	}
}
