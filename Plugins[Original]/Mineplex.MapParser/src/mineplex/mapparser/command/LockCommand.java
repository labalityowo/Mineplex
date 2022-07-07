package mineplex.mapparser.command;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.mapparser.MapData;
import mineplex.mapparser.MapParser;
import org.bukkit.entity.Player;

/**
 *
 */
public class LockCommand extends OpCommand
{

	public LockCommand(MapParser plugin)
	{
		super(plugin, "lock");
	}

	@Override
	public boolean execute(Player player, String alias, String[] args)
	{
		if (player.getWorld().getName().equals("world_lobby"))
		{
			message(player, "Cannot toggle lock for Lobby.");
			return true;
		}

		MapData data = getPlugin().getData(player.getWorld().getName());

		if (data == null)
		{
			player.sendMessage(C.cRed + "There was an error with your map.");
			return true;
		}

		data._locked = !data._locked;
		data.Write();
		message(player, "Lock for world " + F.elem(player.getWorld().getName()) + ": " + F.tf(data._locked));
		return true;
	}
}
