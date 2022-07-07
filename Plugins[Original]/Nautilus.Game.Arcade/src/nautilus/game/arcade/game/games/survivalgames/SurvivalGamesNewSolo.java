package nautilus.game.arcade.game.games.survivalgames;

import java.util.Collections;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.C;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.GameTeam;

public class SurvivalGamesNewSolo extends SurvivalGamesNew
{

	private static final String[] DESCRIPTION =
			{
					"Search for chests to find loot",
					"Slaughter your opponents",
					"Stay away from the borders!",
					"Last tribute alive wins!"
			};

	public SurvivalGamesNewSolo(ArcadeManager manager)
	{
		super(manager, GameType.SurvivalGames, DESCRIPTION);

		DamageTeamSelf = true;
	}

	@EventHandler
	public void customTeamGeneration(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Recruit)
		{
			return;
		}

		GameTeam players = GetTeamList().get(0);
		players.SetColor(ChatColor.YELLOW);
		players.SetName("Tributes");
		players.setDisplayName(C.cYellowB + "Tributes");
	}

	@Override
	public void EndCheck()
	{
		if (!IsLive())
		{
			return;
		}

		if (GetPlayers(true).size() <= 1)
		{
			List<Player> places = GetTeamList().get(0).GetPlacements(true);

			if (places.size() > 0)
			{
				AddGems(places.get(0), 20, "1st Place", false, false);
			}

			if (places.size() > 1)
			{
				AddGems(places.get(1), 15, "2nd Place", false, false);
			}

			if (places.size() > 2)
			{
				AddGems(places.get(2), 10, "3rd Place", false, false);
			}

			for (Player player : GetPlayers(false))
			{
				if (player.isOnline())
				{
					AddGems(player, 10, "Participation", false, false);
				}
			}

			AnnounceEnd(places);
			SetState(GameState.End);
		}
	}

	@Override
	public List<Player> getWinners()
	{
		if (GetState().ordinal() >= GameState.End.ordinal())
		{
			List<Player> places = GetTeamList().get(0).GetPlacements(true);

			if (places.isEmpty() || !places.get(0).isOnline())
			{
				return Collections.emptyList();
			}
			else
			{
				return Collections.singletonList(places.get(0));
			}
		}
		else
		{
			return null;
		}
	}

	@Override
	public List<Player> getLosers()
	{
		List<Player> winners = getWinners();

		if (winners == null)
		{
			return null;
		}

		List<Player> losers = GetPlayers(false);
		losers.removeAll(winners);

		return losers;
	}
}
