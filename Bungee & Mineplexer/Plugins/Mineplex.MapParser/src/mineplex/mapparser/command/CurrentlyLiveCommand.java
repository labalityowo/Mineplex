package mineplex.mapparser.command;

import mineplex.core.common.util.C;
import mineplex.mapparser.MapData;
import mineplex.mapparser.MapParser;
import org.bukkit.entity.Player;

/**
 *
 */
public class CurrentlyLiveCommand extends BaseCommand
{

	public CurrentlyLiveCommand(MapParser plugin, String... aliases)
	{
		super(plugin, "islive", "setlive");
	}

	@Override
	public boolean execute(Player player, String alias, String[] args)
	{
		if (player.getWorld().getName().equals("world_lobby"))
		{
			message(player, "Cannot set live status for Lobby.");
			return true;
		}
		MapData data = getPlugin().getData(player.getWorld().getName());

		if(data == null)
		{
			player.sendMessage(C.cRed + "There was an error with your map.");
			return true;
		}

		if(alias.equalsIgnoreCase("setlive"))
		{
			data._currentlyLive = true;
		}

		player.sendMessage(C.cGray + "Currently Live: " + (data._currentlyLive ? C.cGreen + "True" : C.cRed + "False"));
		return true;
	}
}
