package nautilus.game.arcade.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.C;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.kit.Kit;

public abstract class SoloGame extends Game
{
	protected GameTeam _players;
	
	public SoloGame(ArcadeManager manager, GameType gameType, Kit[] kits, String[] gameDesc) 
	{
		super(manager, gameType, kits, gameDesc);
	}

	@EventHandler
	public void CustomTeamGeneration(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Recruit)
			return;

		_players = GetTeamList().get(0);
		_players.SetColor(ChatColor.YELLOW);
		_players.SetName("Players");
	}
	
	public GameTeam getPlayersTeam()
	{
		return _players;
	}

	@Override
	public void EndCheck()
	{
		if (!IsLive())
			return;

		if (GetPlayers(true).size() <= 1)
		{	
			List<Player> places = _players.GetPlacements(true);
			
			//Announce
			AnnounceEnd(places);

			//Gems
			if (places.size() >= 1)
				AddGems(places.get(0), 20, "1st Place", false, false);

			if (places.size() >= 2)
				AddGems(places.get(1), 15, "2nd Place", false, false);

			if (places.size() >= 3)
				AddGems(places.get(2), 10, "3rd Place", false, false);

			for (Player player : GetPlayers(false))
				if (player.isOnline())
					AddGems(player, 10, "Participation", false, false);

			//End
			SetState(GameState.End);
		}
	}

	@Override
	@EventHandler
	public void ScoreboardUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		if (GetTeamList().isEmpty())
			return;

		Scoreboard.reset();

		Scoreboard.writeNewLine();
		
		GameTeam team = GetTeamList().get(0);

		if (team.GetPlayers(false).size() < 15)
		{
			for (Player player : team.GetPlayers(false))
			{
				if (team.IsAlive(player))
				{
					Scoreboard.write(C.cGreen + player.getName());
				}
				else
				{
					Scoreboard.write(C.cGray + player.getName());
				}
			}
		}
		else if (team.GetPlayers(true).size() < 16)
		{
			for (Player player : team.GetPlayers(true))
			{
				Scoreboard.write(C.cGreen + player.getName());
			}
		}
		else
		{
			Scoreboard.write(C.cGreen + "Players Alive");
			Scoreboard.write("" + team.GetPlayers(true).size());

			Scoreboard.writeNewLine();
			Scoreboard.write(C.cRed + "Players Dead");
			Scoreboard.write("" + (team.GetPlayers(false).size() - team.GetPlayers(true).size()));
		}

		Scoreboard.draw();
	}

	public int GetScoreboardScore(Player player)
	{
		return 0;
	}

	@Override
	public List<Player> getWinners()
	{
		if (GetState().ordinal() >= GameState.End.ordinal())
		{
			List<Player> places = _players.GetPlacements(true);

			if (places.isEmpty() || !places.get(0).isOnline())
				return Arrays.asList();
			else
				return Arrays.asList(places.get(0));
		}
		else
			return null;
	}

	@Override
	public List<Player> getLosers()
	{
		List<Player> winners = getWinners();

		if (winners == null)
			return null;

		List<Player> losers = _players.GetPlayers(false);

		losers.removeAll(winners);

		return losers;
	}
}
