package mineplex.core.elo;

import org.bukkit.entity.Player;

import mineplex.core.command.CommandBase;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.elo.EloManager.EloDivision;
import mineplex.core.game.GameDisplay;
import mineplex.core.slack.SlackAPI;
import mineplex.core.slack.SlackMessage;
import mineplex.core.slack.SlackTeam;

/**
 * Generates a list of top elos
 */
public class TopEloCommand extends CommandBase<EloManager>
{
	public TopEloCommand(EloManager plugin)
	{
		super(plugin, EloManager.Perm.TOP_ELO_COMMAND, "gettopelo", "topelo", "getelo");
	}

	@Override
	public void Execute(Player caller, String[] args)
	{
		if (args.length < 2)
		{
			return;
		}
		String limitRaw = args[1];
		int limit;
		try
		{
			limit = Integer.parseInt(limitRaw);
		}
		catch (NumberFormatException e)
		{
			caller.sendMessage(F.main("Top Elo", "Incorrect number: " + limitRaw + "."));
			return;
		}
		if (limit <= 0)
		{
			caller.sendMessage(F.main("Top Elo", "Incorrect number: " + limitRaw + "."));
			return;
		}
		GameDisplay type;
        try
        {
        	type = GameDisplay.valueOf(args[0]);
        }
        catch (Exception ex)
        {
        	caller.sendMessage(F.main("Top Elo", "Incorrect game: " + args[0] + "."));
            return;
        }
		
		Plugin.getRepo().getTopElo(limit, type.getGameId(), data ->
		{
			caller.sendMessage(C.cAquaB + C.Strike + "=============================================");
			caller.sendMessage(C.cWhite + "Top Elo Data");
			caller.sendMessage(" ");
			StringBuilder builder = new StringBuilder();
			for (int i = 0; i < data.size(); i++)
			{
				StringBuilder messageBuilder = new StringBuilder("#");
				TopEloData topEloData = data.get(i);
				builder.append((i + 1)).append(": ").append(topEloData.getName())
					.append(" ").append(EloDivision.getDivision(topEloData.getElo())).append("").append("\n");
				messageBuilder.append((i + 1)).append(": ").append(topEloData.getName())
					.append(" ").append(EloDivision.getDivision(topEloData.getElo())).append("");
				caller.sendMessage(C.cYellow + messageBuilder.toString());
			}
			SlackMessage slackMessage = new SlackMessage(builder.toString());
			SlackAPI.getInstance().sendMessage(SlackTeam.DEVELOPER, "#top-elo", slackMessage, false);
			caller.sendMessage(" ");
			caller.sendMessage(C.cAquaB + C.Strike + "=============================================");
		});
	}
}