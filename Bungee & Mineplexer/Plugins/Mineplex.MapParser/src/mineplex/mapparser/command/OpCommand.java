package mineplex.mapparser.command;

import org.bukkit.entity.Player;

import mineplex.mapparser.MapParser;

public abstract class OpCommand extends BaseCommand
{
	public OpCommand(MapParser plugin, String... aliases)
	{
		super(plugin, aliases);
	}

	@Override
	public boolean canRun(Player player)
	{
		return player.isOp();
	}
}
