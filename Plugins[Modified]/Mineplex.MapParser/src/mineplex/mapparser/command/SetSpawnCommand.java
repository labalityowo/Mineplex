package mineplex.mapparser.command;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import mineplex.mapparser.MapParser;

public class SetSpawnCommand extends BaseCommand
{

	public SetSpawnCommand(MapParser plugin)
	{
		super(plugin, "setspawn");
	}

	@Override
	public boolean execute(Player player, String alias, String[] args)
	{
		Location loc = player.getLocation();

		player.getWorld().setSpawnLocation(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());

		player.sendMessage(ChatColor.YELLOW + "Spawn saved for world '" + ChatColor.GOLD + loc.getWorld().getName()
				+ ChatColor.YELLOW + "' to X: " + ChatColor.GOLD + loc.getBlockX() + ChatColor.YELLOW + ", Y: "
				+ ChatColor.GOLD + loc.getBlockY() + ChatColor.YELLOW + ", Z: " + ChatColor.GOLD + loc.getBlockZ());

		return true;
	}

}
