package mineplex.mapparser.command;

import org.bukkit.entity.Player;

import mineplex.mapparser.MapData;
import mineplex.mapparser.MapParser;
import mineplex.mapparser.command.BaseCommand;

public class MapInfoCommand extends BaseCommand
{
	public MapInfoCommand(MapParser plugin)
	{
		super(plugin, "mapinfo", "info");
	}

	@Override
	public boolean execute(Player player, String alias, String[] args)
	{
		MapData data = getPlugin().getData(player.getWorld().getName());

		data.sendInfo(player);
		return true;
	}
}
