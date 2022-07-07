package mineplex.game.nano.commands.game;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.game.nano.NanoManager;
import mineplex.game.nano.NanoManager.Perm;
import mineplex.game.nano.game.GameType;

public class GameSetCommand extends CommandBase<NanoManager>
{

	GameSetCommand(NanoManager plugin)
	{
		super(plugin, Perm.GAME_COMMAND, "set");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length < 1)
		{
			caller.sendMessage(F.main(Plugin.getName(), "Enter a valid game."));
			return;
		}

		String game = args[0].toUpperCase();
		String map = null;

		if (args.length > 1)
		{
			map = Arrays.stream(args)
					.skip(1)
					.collect(Collectors.joining(" "));
		}

		GameType gameType;

		try
		{
			gameType = GameType.valueOf(game);
		}
		catch (IllegalArgumentException e)
		{
			caller.sendMessage(F.main(Plugin.getName(), F.name(game) + " is not a valid game."));
			return;
		}

		Bukkit.broadcastMessage(C.cAquaB + caller.getName() + " set the next game to " + gameType.getName() + (map != null ? " and map to " + map : ""));
		Plugin.getGameCycle().setNextGameMap(gameType, map);
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String commandLabel, String[] args)
	{
		if (args.length == 0)
		{
			return Arrays.stream(GameType.values())
					.map(Enum::name)
					.collect(Collectors.toList());
		}
		else if (args.length == 1)
		{
			return Arrays.stream(GameType.values())
					.map(Enum::name)
					.filter(s -> s.startsWith(args[0].toUpperCase()))
					.collect(Collectors.toList());
		}

		return null;
	}
}
