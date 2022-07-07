package mineplex.mapparser.command;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import mineplex.mapparser.MapParser;
import mineplex.mapparser.command.teleport.TeleportManager;

/**
 * Created by Shaun on 8/15/2014.
 */
public class HubCommand extends BaseCommand
{
	private TeleportManager _teleportManager;

	public HubCommand(TeleportManager teleportManager)
	{
		super(teleportManager.getPlugin(), "hub");

		_teleportManager = teleportManager;
	}

	@Override
	public boolean execute(Player player, String alias, String[] args)
	{
		_teleportManager.teleportPlayer(player, getPlugin().getSpawnLocation());
		return true;
	}
}
