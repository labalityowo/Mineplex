package mineplex.mapparser.command;

import org.bukkit.World;
import org.bukkit.entity.Player;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayerBase;
import mineplex.mapparser.MapData;
import mineplex.mapparser.MapParser;

/**
 * Created by Shaun on 8/16/2014.
 */
public class AdminCommand extends MapAdminCommand
{
	public AdminCommand(MapParser plugin)
	{
		super(plugin, "admin");
	}

	@Override
	public boolean execute(Player player, String alias, String[] args)
	{
		if (args.length != 1)
		{
			message(player, "Invalid Input. " + F.elem("/admin <Name>"));
			return true;
		}

		World world = player.getWorld();

		if (world.getName().equals("world_lobby"))
		{
			message(player, "Cannot change Admin-List for Lobby.");
			return true;
		}

		Player other = UtilPlayerBase.searchOnline(player, args[0], true);

		if (other != null)
		{
			MapData data = getPlugin().getData(world.getName());

			if (data.AdminList.contains(other.getName()))
			{
				data.AdminList.remove(other.getName());
				data.Write();

				getPlugin().announce("Admin-List for " + F.elem(world.getName()) + "  (" + other.getName() + " = " + F.tf(false) + ")");
			}
			else
			{
				data.AdminList.add(other.getName());
				data.Write();

				getPlugin().announce("Admin-List for " + F.elem(world.getName()) + "  (" + other.getName() + " = " + F.tf(true) + ")");
			}
		}
		else
		{
			player.sendMessage(F.main(getPlugin().getName(), "That player is not online."));
		}

		return true;
	}
}
