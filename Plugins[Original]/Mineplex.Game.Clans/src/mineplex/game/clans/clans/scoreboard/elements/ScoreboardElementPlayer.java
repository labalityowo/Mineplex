package mineplex.game.clans.clans.scoreboard.elements;

import java.util.List;

import org.bukkit.entity.Player;

import com.google.common.collect.Lists;

import mineplex.core.common.util.C;
import mineplex.core.thereallyoldscoreboardapiweshouldremove.ScoreboardManager;
import mineplex.core.thereallyoldscoreboardapiweshouldremove.elements.ScoreboardElement;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.ClansUtility;
import mineplex.game.clans.core.repository.ClanTerritory;

public class ScoreboardElementPlayer implements ScoreboardElement
{
	private ClansManager _clansManager;

	public ScoreboardElementPlayer(ClansManager clansManager)
	{
		_clansManager = clansManager;
	}

	@Override
	public List<String> getLines(ScoreboardManager manager, Player player, List<String> out)
	{
		List<String> output = Lists.newArrayList();
		output.add(C.cYellowB + "Gold");
		output.add(C.cGold + _clansManager.getGoldManager().Get(player).getBalance());

		output.add("   ");
		
		output.add(C.cYellowB + "Territory");
		String regionString = C.xWilderness + "Wilderness";
		ClanTerritory claim = _clansManager.getClanUtility().getClaim(player.getLocation());
		if (claim != null)
		{
			//Relation
			ClansUtility.ClanRelation relation = _clansManager.getClanUtility().relPT(player, claim.ClaimLocation);

			//Name
			regionString = _clansManager.getClanUtility().mRel(relation, claim.Owner, false);

			//Trust
			if (relation == ClansUtility.ClanRelation.ALLY_TRUST)
			{
				regionString += C.mBody + "(" + C.mElem + "Trusted" + C.mBody + ")";
			}
		}
		
		if (_clansManager.getNetherManager().isInNether(player))
		{
			regionString = C.cClansNether + "The Nether";
			if (_clansManager.getClanUtility().isSafe(player.getLocation()))
			{
				regionString = C.cClansNether + "Nether Spawn";
			}
		}
		
		if (_clansManager.getWorldEvent().getRaidManager().isInRaid(player.getLocation()))
		{
			regionString = C.cDRed + "Raid World";
		}
		
		output.add(regionString);
		
		return output;
	}
}