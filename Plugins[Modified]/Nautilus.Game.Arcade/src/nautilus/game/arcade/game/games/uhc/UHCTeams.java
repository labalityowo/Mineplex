package nautilus.game.arcade.game.games.uhc;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerQuitEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.team.TeamRequestsModule;

public class UHCTeams extends UHC
{

	public UHCTeams(ArcadeManager manager)
	{
		this(manager, GameType.UHC);
	}

	public UHCTeams(ArcadeManager manager, GameType type)
	{
		this(manager, type, false);
	}
	
	public UHCTeams(ArcadeManager manager, GameType type, boolean speedMode)
	{
		super(manager, type, speedMode);

		DamageTeamSelf = false;
 		ShowTeammateMessage = true;
 		TeamMode = true;
		
		// Load the Team Module
		new TeamRequestsModule().register(this);
	}

	@EventHandler(priority = EventPriority.LOW)
	public void PlayerQuit(PlayerQuitEvent event)
	{
		if (!InProgress())
			return;

		Player player = event.getPlayer();

		GameTeam team = GetTeam(player);
		if (team == null) return;

		if (!team.IsAlive(player))
			return;

		team.RemovePlayer(player);
	}
	
	@EventHandler
	public void TeamRename(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Live)
		{
			return;
		}
		
		for (GameTeam team : GetTeamList())
		{
			// Big Team
			if (team.GetSize() > 2)
			{
				team.SetName("Team " + team.GetName());
				continue;
			}

			String name = "";
			List<Player> players = team.GetPlayers(false);
			
			for (int i = 0; i < players.size(); i++)
			{
				Player player = players.get(i);

				name += player.getName();

				if (i < players.size() - 1)
				{
					name += " & ";
				}
			}

			team.SetName(name);
		}
	}

	@Override
	public List<Player> getWinners()
	{
		if (WinnerTeam == null)
			return null;

		return WinnerTeam.GetPlayers(false);
	}

	@Override
	public List<Player> getLosers()
	{
		if (WinnerTeam == null)
			return null;

		List<Player> players = new ArrayList<>();

		for (GameTeam team : GetTeamList())
		{
			if (team != WinnerTeam)
				players.addAll(team.GetPlayers(false));
		}

		return players;
	}

	@Override
	public String GetMode()
	{
		return "UHC Teams";
	}

}
