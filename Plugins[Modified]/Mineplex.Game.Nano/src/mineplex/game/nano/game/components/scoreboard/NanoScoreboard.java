package mineplex.game.nano.game.components.scoreboard;

import net.md_5.bungee.api.ChatColor;

import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import mineplex.core.common.util.C;
import mineplex.core.scoreboard.WritableMineplexScoreboard;
import mineplex.core.titles.tracks.custom.ScrollAnimation;
import mineplex.game.nano.game.components.team.GameTeam;

public class NanoScoreboard extends WritableMineplexScoreboard
{

	private static final String[] TITLE = new ScrollAnimation("  MINEPLEX  ")
			.withPrimaryColour(ChatColor.GOLD)
			.withSecondaryColour(ChatColor.YELLOW)
			.withTertiaryColour(ChatColor.WHITE)
			.bold()
			.build();
	private static final String SPEC_TEAM = "SPEC";

	private final GameScoreboardComponent _manager;

	private int _shineIndex;

	NanoScoreboard(GameScoreboardComponent manager, Player player)
	{
		super(player);

		_manager = manager;
		setSidebarName(TITLE[0]);
	}

	@Override
	public void draw()
	{
		while (_bufferedLines.size() > 15)
		{
			_bufferedLines.remove(_bufferedLines.size() - 1);
		}

		super.draw();
	}

	void updateTitle()
	{
		setSidebarName(TITLE[_shineIndex]);
		_shineIndex = (_shineIndex + 1) % TITLE.length;
	}

	void refreshAsSubject(Player player)
	{
		updateTabList(player);
		updateUnderName(player);
	}

	void setPlayerTeam(Player subject, GameTeam team)
	{
		if (team == null)
		{
			setSpectating(subject);
			return;
		}

		String teamId = team.getName();
		Team scoreboardTeam = getHandle().getTeam(teamId);

		if (scoreboardTeam == null)
		{
			scoreboardTeam = getHandle().registerNewTeam(teamId);
			scoreboardTeam.setPrefix(_manager.getPrefix(getOwner(), team));
			scoreboardTeam.setSuffix(_manager.getSuffix(getOwner(), team));

			if (_manager.getSetupSettingsConsumer() != null)
			{
				_manager.getSetupSettingsConsumer().accept(getOwner(), team, scoreboardTeam);
			}
		}

		setPlayerTeam(subject, teamId);
	}

	private void setSpectating(Player subject)
	{
		Team specTeam = getHandle().getTeam(SPEC_TEAM);

		if (specTeam == null)
		{
			specTeam = getHandle().registerNewTeam(SPEC_TEAM);
			specTeam.setPrefix(C.cGray);
		}

		setPlayerTeam(subject, SPEC_TEAM);
	}

	private void setPlayerTeam(Player subject, String teamId)
	{
		String entry = subject.getName();

		for (Team team : getHandle().getTeams())
		{
			team.removeEntry(entry);
		}

		getHandle().getTeam(teamId).addEntry(entry);
	}

	private void updateTabList(Player subject)
	{
		Integer value = _manager.getTabList(getOwner(), subject);

		if (value == null)
		{
			return;
		}

		Objective objective = getHandle().getObjective(DisplaySlot.PLAYER_LIST);

		if (objective == null)
		{
			objective = getHandle().registerNewObjective("TabList", "dummy");
			objective.setDisplaySlot(DisplaySlot.PLAYER_LIST);
		}

		Score score = objective.getScore(subject.getName());

		if (score.getScore() != value)
		{
			score.setScore(value);
		}
	}

	private void updateUnderName(Player subject)
	{
		if (_manager.getUnderNameObjective() == null)
		{
			return;
		}

		Scoreboard handle = getHandle();
		Objective objective = handle.getObjective(DisplaySlot.BELOW_NAME);
		int value = _manager.getUnderName(getOwner(), subject);

		if (objective == null)
		{
			objective = handle.registerNewObjective(_manager.getUnderNameObjective(), "dummy");
			objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
		}

		Score score = objective.getScore(subject.getName());

		if (score.getScore() != value)
		{
			score.setScore(value);
		}
	}
}
