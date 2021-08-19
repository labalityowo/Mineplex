package mineplex.core.thereallyoldscoreboardapiweshouldremove;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.C;

public class PlayerScoreboard
{
	private ScoreboardManager _manager;

	private String _scoreboardData = "default";

	private Scoreboard _scoreboard;
	private Objective _sideObjective;

	private List<String> _currentLines = new ArrayList<>();

	private String[] _teamNames;

	public PlayerScoreboard(ScoreboardManager manager, Player player)
	{
		_manager = manager;
	}

	protected void addTeams(Player player)
	{
		_scoreboard.registerNewTeam("Vanished").setSuffix(C.cBlue + "*");
		
		for (PermissionGroup group : PermissionGroup.values())
		{
			if (!group.canBePrimary())
			{
				continue;
			}
			if (!group.getDisplay(false, false, false, false).isEmpty())
			{
				_scoreboard.registerNewTeam(group.name()).setPrefix(group.getDisplay(true, true, true, false) + ChatColor.RESET + " ");
			}
			else
			{
				_scoreboard.registerNewTeam(group.name()).setPrefix("");
			}
		}

		_scoreboard.registerNewTeam("Party").setPrefix(ChatColor.LIGHT_PURPLE + C.Bold + "Party" + ChatColor.RESET + " ");

		for (Player otherPlayer : Bukkit.getOnlinePlayers())
		{
			if (otherPlayer.equals(player))
			{
				continue;
			}

			if (_manager.getClients().Get(otherPlayer) == null)
			{
				continue;
			}

			String rankName = _manager.getClients().Get(player).getRealOrDisguisedPrimaryGroup().name();
			String otherRankName = _manager.getClients().Get(otherPlayer).getRealOrDisguisedPrimaryGroup().name();

			//Add Other to Self
			_scoreboard.getTeam(otherRankName).addPlayer(otherPlayer);

			//Add Self to Other
			otherPlayer.getScoreboard().getTeam(rankName).addPlayer(player);
			
			if (_manager.getIncognitoManager().Get(otherPlayer).Hidden)
			{
				_scoreboard.getTeam("Vanished").addPlayer(otherPlayer);
			}
			
			if (_manager.getIncognitoManager().Get(player).Hidden)
			{
				_scoreboard.getTeam("Vanished").addPlayer(player);
			}
		}
	}

	private ScoreboardData getData()
	{
		ScoreboardData data = _manager.getData(_scoreboardData, false);
		if (data != null)
			return data;

		//Revert to default
		_scoreboardData = "default";
		return _manager.getData(_scoreboardData, false);
	}

	public void draw(ScoreboardManager manager, Player player)
	{
		ScoreboardData data = getData();

		if (data == null)
			return;

		List<String> lines = data.getLines(manager, player);

		for (int i=0 ; i<lines.size() ; i++)
		{
			if ((15-i) >= 0)
			{
				//Get New Line
				String newLine = lines.get(i);

				//Check if Unchanged
				if (_currentLines.size() > i)
				{
					String oldLine = _currentLines.get(i);

					if (oldLine.equals(newLine))
						continue;
				}

				// Ignore extra lines
				if (i >= _teamNames.length)
					continue;

				//Update
				Team team = _scoreboard.getTeam(_teamNames[i]);
				if (team == null)
				{
					System.out.println("Scoreboard Error: Line Team Not Found!");
					return;
				}

				//Set Line Prefix/Suffix
				team.setPrefix(newLine.substring(0, Math.min(newLine.length(), 16)));
				team.setSuffix(ChatColor.getLastColors(team.getPrefix()) + newLine.substring(team.getPrefix().length(), Math.min(newLine.length(), 32)));

				//Line
				_sideObjective.getScore(_teamNames[i]).setScore(15-i);
			}
		}

		//Hide Old Unused
		if (_currentLines.size() > lines.size())
		{
			for (int i=lines.size() ; i<_currentLines.size() ; i++)
			{
				if (i < _teamNames.length)
				{
					_scoreboard.resetScores(_teamNames[i]);
				}
			}
		}

		//Save New State
		_currentLines = lines;
	}

	public void setTitle(String out)
	{
		_sideObjective.setDisplayName(out);
	}

	public void assignScoreboard(Player player)
	{
		assignScoreboard(player, getData());
	}

	public void assignScoreboard(Player player, ScoreboardData data)
	{
		//Scoreboard
		_scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();

		//Side Obj
		_sideObjective = _scoreboard.registerNewObjective("side", "dummy");
		_sideObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
		_sideObjective.setDisplayName(C.Bold + "   MINEPLEX   ");

		//Teams
		addTeams(player);

		//Create Line Teams - There will always be 16 teams, with static line allocations.
		_teamNames = new String[16];
		for (int i=0 ; i<16 ; i++)
		{
			String teamName = ChatColor.COLOR_CHAR + "" + ("1234567890abcdefghijklmnopqrstuvwxyz".toCharArray())[i] + ChatColor.RESET;

			_teamNames[i] = teamName;

			Team team = _scoreboard.registerNewTeam(teamName);
			team.addEntry(teamName);
		}

		
		
		//
//		if (data.getDisplayRanks())
//		for (Player otherPlayer : Bukkit.getOnlinePlayers())
//		{
//			if (_clientManager.Get(otherPlayer) == null)
//				continue;
//
//			String rankName = _clientManager.Get(player).GetRank().Name;
//			String otherRankName = _clientManager.Get(otherPlayer).GetRank().Name;
//
//			if (!_clientManager.Get(player).GetRank().Has(Rank.ULTRA) && _donationManager.Get(player.getName()).OwnsUltraPackage())
//			{
//				rankName = Rank.ULTRA.Name;
//			}
//
//			if (!_clientManager.Get(otherPlayer).GetRank().Has(Rank.ULTRA) && _donationManager.Get(otherPlayer.getName()).OwnsUltraPackage())
//			{
//				otherRankName = Rank.ULTRA.Name;
//			}
//
//			//Add Other to Self
//			board.getTeam(otherRankName).addPlayer(otherPlayer);
//		}
		
		//Set Scoreboard
		player.setScoreboard(_scoreboard);
	}

	protected Scoreboard getScoreboard()
	{
		return _scoreboard;
	}
}