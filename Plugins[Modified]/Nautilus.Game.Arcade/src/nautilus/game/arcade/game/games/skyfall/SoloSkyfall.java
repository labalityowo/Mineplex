package nautilus.game.arcade.game.games.skyfall;

import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilTime;
import mineplex.core.common.util.UtilTime.TimeUnit;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.GameTeam;

/**
 * SoloSkyfall
 *
 * @author xXVevzZXx
 */
public class SoloSkyfall extends Skyfall
{
	private GameTeam _players;

	public SoloSkyfall(ArcadeManager manager)
	{
		super(manager, GameType.Skyfall);
		
		this.DamageTeamSelf = true;	
	}
	
	@EventHandler
	public void CustomTeamGeneration(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Recruit)
			return;

		_players = GetTeamList().get(0);
		_players.SetColor(ChatColor.YELLOW);
		_players.SetName("Players");
		_players.setDisplayName(C.cYellow + C.Bold + "Players");
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

		GameTeam team = GetTeamList().get(0);

		if (IsLive())
		{
			Scoreboard.writeNewLine();
			Scoreboard.write(C.cGreen + C.Bold + "Time");
			Scoreboard.write(UtilTime.convertString(System.currentTimeMillis() - GetStateTime(), 0, TimeUnit.FIT));
			Scoreboard.writeNewLine();
		}

		Scoreboard.write(C.cYellow + C.Bold + "Players");
		if (team.GetPlayers(true).size() > 7)
		{
			Scoreboard.write("" + team.GetPlayers(true).size());
		}
		else
		{
			for (Player player : team.GetPlayers(true))
			{
				Scoreboard.write(C.cWhite + player.getName());
			}
		}
		
		if (IsLive() && !isDeathMatch())
		{
			Scoreboard.writeNewLine();
			Scoreboard.write(C.cGold + C.Bold + "Chest Refill");
			Scoreboard.write(C.cWhite + UtilTime.MakeStr((getChestsRefilled() + getChestRefillTime()) - System.currentTimeMillis()));
		}
		else if (isDeathMatch() && !isDeathMatchStarted())
		{
			Scoreboard.writeNewLine();
			Scoreboard.write(C.cRed + C.Bold + "Deathmatch");
			Scoreboard.write(F.time(UtilTime.MakeStr(isTeleportedDeathmatch() ? (getDeathmatchStartTime() + getDeathmatchStartingTime() + getDeathmatchWaitTime()) - System.currentTimeMillis() : (getDeathmatchStartTime() + getDeathmatchStartingTime()) - System.currentTimeMillis())));
		}
		else if (isDeathMatchStarted())
		{
			Scoreboard.writeNewLine();
			Scoreboard.write(C.cRed + C.Bold + "Game End");
			Scoreboard.write(UtilTime.convertString(Math.max(0, (GetStateTime() + GameTimeout) - System.currentTimeMillis()), 0, TimeUnit.FIT));
		}
		
		Scoreboard.draw();
	}
	
	@Override
	public void EndCheck()
	{
		if (!IsLive())
			return;

		if (GetPlayers(true).size() <= 1)
		{	
			List<Player> places = GetTeamList().get(0).GetPlacements(true);
			
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
	public List<Player> getWinners()
	{
		if (GetState().ordinal() >= GameState.End.ordinal())
		{
			List<Player> places = GetTeamList().get(0).GetPlacements(true);

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

		List<Player> losers = GetTeamList().get(0).GetPlayers(false);

		losers.removeAll(winners);

		return losers;
	}

	@Override
	public String GetMode()
	{
		return "Solo Mode";
	}

}
