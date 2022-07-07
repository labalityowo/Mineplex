package mineplex.game.nano.game.games.quick;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.F;
import mineplex.game.nano.NanoManager;
import mineplex.game.nano.game.games.quick.Quick.Perm;

public class ChallengeSetCommand extends CommandBase<NanoManager>
{

	ChallengeSetCommand(NanoManager plugin)
	{
		super(plugin, Perm.CHALLENGE_SET_COMMAND, "chalset");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		Quick game = (Quick) Plugin.getGame();

		try
		{
			List<ChallengeType> challenges = Arrays.stream(args)
					.map(arg -> ChallengeType.valueOf(arg.toUpperCase()))
					.collect(Collectors.toList());

			caller.sendMessage(F.main(Plugin.getName(), "Set the challenges to: " + challenges));
			game.setChallenges(challenges);
		}
		catch (Exception e)
		{
			caller.sendMessage(F.main(Plugin.getName(), "Invalid ChallengeType."));
		}
	}
}
