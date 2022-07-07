package mineplex.mapparser.command;

import org.bukkit.entity.Player;

import mineplex.mapparser.MapParser;
import mineplex.mapparser.command.teleport.TeleportManager;

public class SpawnCommand extends BaseCommand
{
	private TeleportManager _teleportManager;

	public SpawnCommand(TeleportManager plugin)
	{
		super(plugin.getPlugin(), "spawn");

		_teleportManager = plugin;
	}

	@Override
	public boolean execute(Player player, String alias, String[] args)
	{
		_teleportManager.teleportPlayer(player, player.getWorld().getSpawnLocation());
		return true;
	}

}
