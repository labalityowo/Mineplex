package mineplex.game.clans.clans.scoreboard.elements;

import java.util.List;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilTime;
import mineplex.core.thereallyoldscoreboardapiweshouldremove.ScoreboardManager;
import mineplex.core.thereallyoldscoreboardapiweshouldremove.elements.ScoreboardElement;
import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.ClansManager;

import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

public class ScoreboardElementClan implements ScoreboardElement
{
	private ClansManager _clansManager;

	public ScoreboardElementClan(ClansManager clansManager)
	{
		_clansManager = clansManager;
	}

	@Override
	public List<String> getLines(ScoreboardManager manager, Player player, List<String> out)
	{
		List<String> output = Lists.newArrayList();

		ClanInfo clanInfo = _clansManager.getClan(player);

		if (clanInfo != null)
		{
			output.add(C.cYellowB + "Clan");
			output.add(_clansManager.getClanUtility().mRel(_clansManager.getRelation(player, player), clanInfo.getName(), false));
			output.add(" ");
			// Energy
			if (clanInfo.getEnergyCostPerMinute() > 0)
			{
				output.add(C.cYellowB + "Clan Energy");
				output.add(C.cGreen + UtilTime.convertString((clanInfo.getEnergy() / clanInfo.getEnergyCostPerMinute()) * 60000L, 1, UtilTime.TimeUnit.FIT));
				output.add("  ");
			}
		}
		else
		{
			output.add(C.cYellowB + "Clan");
			output.add("No Clan");
			output.add(" ");
		}

		return output;
	}
}
