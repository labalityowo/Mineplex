package mineplex.game.clans.clans.scoreboard.elements;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilServer;
import mineplex.core.thereallyoldscoreboardapiweshouldremove.ScoreboardManager;
import mineplex.core.thereallyoldscoreboardapiweshouldremove.elements.ScoreboardElement;
import mineplex.game.clans.clans.ClansManager;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class ScoreboardElementPlayerCount implements ScoreboardElement
{
	private ClansManager _clansManager;

	public ScoreboardElementPlayerCount(ClansManager clansManager)
	{
		_clansManager = clansManager;
	}

	@Override
	public List<String> getLines(ScoreboardManager manager, Player player, List<String> out)
	{
		List<String> output = new ArrayList<String>();
		
		output.add("");
		output.add(C.cYellow + "Players " + C.cWhite + UtilServer.getPlayers().length + "/100");

		return output;
	}
}
