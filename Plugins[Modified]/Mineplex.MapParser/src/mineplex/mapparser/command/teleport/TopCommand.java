package mineplex.mapparser.command.teleport;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.mapparser.command.BaseCommand;

public class TopCommand extends BaseCommand
{
	private TeleportManager _teleportManager;

	public TopCommand(TeleportManager teleportManager)
	{
		super(teleportManager.getPlugin(), "top");

		_teleportManager = teleportManager;
	}

	@Override
	public boolean execute(Player player, String alias, String[] args)
	{
		Location destination = player.getLocation().clone();

		destination.setY(256);

		while (destination.getBlock().getType().equals(Material.AIR))
		{
			destination.add(0, -1, 0);
		}

		_teleportManager.teleportPlayer(player, destination.add(0, 1, 0));

		message(player, "You have been teleported to  Y = " + C.cYellow + destination.getY());

		return true;
	}
}
