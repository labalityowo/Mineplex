package mineplex.mapparser.command;

import java.util.Arrays;
import java.util.stream.Collectors;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.mapparser.MapParser;
import org.bukkit.entity.Player;

/**
 *
 */
public class PMCommand extends OpCommand
{
	public PMCommand(MapParser plugin)
	{
		super(plugin, "m", "message");
	}

	@Override
	public boolean execute(Player player, String alias, String[] args)
	{
		if (args.length == 0)
		{
			player.sendMessage(F.main(getPlugin().getName(), "Please put a message in!"));
			return true;
		}

		String message = C.cDRed + "OP " + player.getName() + " " + C.cPurple + Arrays.stream(args).collect(Collectors.joining(" ")).trim();

		getPlugin().getServer().getOnlinePlayers().stream().filter(Player::isOp).forEach(p -> p.sendMessage(message));

		return true;
	}
}