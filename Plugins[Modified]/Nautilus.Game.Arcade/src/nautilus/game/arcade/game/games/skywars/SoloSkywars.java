package nautilus.game.arcade.game.games.skywars;

import java.util.Collections;
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
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.skywars.trackers.TNTStatTracker;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.stats.DeathBomberStatTracker;
import nautilus.game.arcade.stats.WinWithoutOpeningChestStatTracker;
import nautilus.game.arcade.stats.WinWithoutWearingArmorStatTracker;

public class SoloSkywars extends Skywars
{

	@SuppressWarnings("unchecked")
	public SoloSkywars(ArcadeManager manager)
	{
		this(manager, GameType.Skywars);

		registerStatTrackers
				(
						new TNTStatTracker(this),
						new DeathBomberStatTracker(this, 3), //TNT Kills
						new WinWithoutOpeningChestStatTracker(this),
						new WinWithoutWearingArmorStatTracker(this)
				);

		registerChatStats
				(
						Kills,
						Deaths,
						KDRatio,
						BlankLine,
						Assists,
						DamageTaken,
						DamageDealt
				);
	}

	public SoloSkywars(ArcadeManager manager, GameType type)
	{
		super(manager, type,
				new String[]
						{
								"Free for all battle in the sky!",
								"Craft or loot gear for combat",
								"Last player alive wins!"
						});

		DamageTeamSelf = true;
	}

	public SoloSkywars(ArcadeManager manager, Kit[] kits, GameType type)
	{
		super(manager, kits, type,
				new String[]
						{
								"Free for all battle in the sky!",
								"Craft or loot gear for combat",
								"Last player alive wins!"
						});

		DamageTeamSelf = true;
	}

	@EventHandler
	public void CustomTeamGeneration(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Recruit)
		{
			return;
		}

		GameTeam players = GetTeamList().get(0);
		players.SetColor(ChatColor.YELLOW);
		players.SetName("Players");
		players.setDisplayName(C.cYellow + C.Bold + "Players");
	}

	@Override
	@EventHandler
	public void ScoreboardUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST || !InProgress())
		{
			return;
		}

		GameTeam team = GetTeamList().get(0);

		Scoreboard.writeNewLine();
		Scoreboard.write(C.cYellow + C.Bold + "Players");
		if (team.GetPlayers(true).size() > 10)
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

		writeScoreboard();

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
				return Collections.emptyList();
			else
				return Collections.singletonList(places.get(0));
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
