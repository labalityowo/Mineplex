package mineplex.game.clans.clans.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilText;
import mineplex.core.thereallyoldscoreboardapiweshouldremove.PlayerScoreboard;
import mineplex.core.thereallyoldscoreboardapiweshouldremove.ScoreboardManager;
import mineplex.game.clans.Clans;
import mineplex.game.clans.clans.ClanInfo;
import mineplex.game.clans.clans.ClansManager;
import mineplex.game.clans.clans.ClansUtility.ClanRelation;

public class ClansPlayerScoreboard extends PlayerScoreboard
{
	private ClansManager _clansManager;

	public ClansPlayerScoreboard(ScoreboardManager manager, ClansManager clansManager, Player player)
	{
		super(manager, player);
		_clansManager = clansManager;
	}

	@Override
	protected void addTeams(Player player)
	{
		refreshTeams(player);
	}

	public void refreshTeams(Player player)
	{
		System.out.println("<--> SB refresh");
		for (Player otherPlayer : Bukkit.getOnlinePlayers())
		{
			if (otherPlayer.equals(player))
			{
				ClanInfo clan = _clansManager.getClan(player);
				ClanRelation rel = _clansManager.getClanUtility().rel(clan, clan);
				// Add Self to Self
				add(getScoreboard(), otherPlayer, clan, rel, 0);
			}
			else
			{
				ClanInfo clan = _clansManager.getClan(player);
				ClanInfo otherClan = _clansManager.getClan(otherPlayer);
				ClanRelation rel = _clansManager.getClanUtility().rel(clan, otherClan);
				int clanScore = (clan != null && otherClan != null) ? clan.getWarPoints(otherClan) : 0;
				int otherClanScore = (clan != null && otherClan != null) ? otherClan.getWarPoints(clan) : 0;

				// Add Other to Self
				add(getScoreboard(), otherPlayer, otherClan, rel, clanScore);
				// Add Self to Other
				add(otherPlayer.getScoreboard(), player, clan, rel, otherClanScore);
			}
		}
	}
	
	public void removeFromAllTeams(Scoreboard scoreboard, Player player)
	{
		for (Team team : scoreboard.getTeams())
		{
			if (team.hasPlayer(player))
			{
				team.removePlayer(player);
			}
		}
	}

	public void add(Scoreboard scoreboard, Player otherPlayer, ClanInfo clanInfo, ClanRelation relation, int ownScore)
	{
		if (otherPlayer.getGameMode().equals(GameMode.CREATIVE))
		{
			String teamName = UtilText.trim(16, _clansManager.getClientManager().Get(otherPlayer).getPrimaryGroup().name() + "CREATIVE");
			Team team = scoreboard.getTeam(teamName);
			if (team == null)
			{
				team = scoreboard.registerNewTeam(teamName);
				if (!_clansManager.getClientManager().Get(otherPlayer).getPrimaryGroup().getDisplay(false, false, false, false).isEmpty())
				{
					team.setPrefix(UtilText.trim(16, _clansManager.getClientManager().Get(otherPlayer).getPrimaryGroup().getDisplay(true, true, true, false) + ChatColor.RESET + " "));
				}
				team.setSuffix(C.cRed + " STAFF MODE");
			}

			Objective domObjective;
			if ((domObjective = scoreboard.getObjective(DisplaySlot.BELOW_NAME)) == null)
			{
				domObjective = scoreboard.registerNewObjective("war", "dummy");
				domObjective.setDisplayName("War Points");
				domObjective.setDisplaySlot(DisplaySlot.BELOW_NAME);
			}

			if (clanInfo != null)
				domObjective.getScore(otherPlayer.getName()).setScore(ownScore);

			team.addPlayer(otherPlayer);
			
			return;
		}
		
		String teamName = getTeamName(clanInfo, relation, ownScore);
		Team team = scoreboard.getTeam(teamName);
		if (team == null)
		{
			team = scoreboard.registerNewTeam(teamName);
			if (clanInfo != null)
				team.setPrefix(relation.getColor(true) + clanInfo.getName() + relation.getColor(false) + " ");
			else
				team.setPrefix(relation.getColor(false).toString());
		}
		
		if (Clans.HARDCORE)
		{
			Objective domObjective;
			if ((domObjective = scoreboard.getObjective(DisplaySlot.BELOW_NAME)) == null)
			{
				domObjective = scoreboard.registerNewObjective("war", "dummy");
				domObjective.setDisplayName("War Points");
				domObjective.setDisplaySlot(DisplaySlot.BELOW_NAME);
			}

			if (clanInfo != null)
				domObjective.getScore(otherPlayer.getName()).setScore(ownScore);
		}

		team.addPlayer(otherPlayer);
	}

	public String getTeamName(ClanInfo clanInfo, ClanRelation relation, int ownScore)
	{
		return (clanInfo == null ? "" : clanInfo.getId() + "") + relation.ordinal();
	}
}