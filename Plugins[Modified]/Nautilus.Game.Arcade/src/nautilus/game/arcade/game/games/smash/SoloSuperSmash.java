package nautilus.game.arcade.game.games.smash;

import java.util.Arrays;
import java.util.List;

import mineplex.core.common.Pair;
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
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.managers.chat.ChatStatData;
import nautilus.game.arcade.stats.FreeKitWinStatTracker;
import nautilus.game.arcade.stats.KillFastStatTracker;
import nautilus.game.arcade.stats.OneVThreeStatTracker;
import nautilus.game.arcade.stats.RecoveryMasterStatTracker;
import nautilus.game.arcade.stats.WinWithoutDyingStatTracker;

public class SoloSuperSmash extends SuperSmash
{

	private GameTeam _players;
	
	public SoloSuperSmash(ArcadeManager manager)
	{
		super(manager, GameType.Smash, new String[]
				{
				"Each player has 3 respawns",
				"Attack to restore hunger!",
				"Last player alive wins!"
					});
		
		this.DamageTeamSelf = true;

		registerStatTrackers(
				new WinWithoutDyingStatTracker(this, "MLGPro"),
				new FreeKitWinStatTracker(this),
				new OneVThreeStatTracker(this),
				new KillFastStatTracker(this, 3, 10, "TripleKill"),
				new RecoveryMasterStatTracker(this)
				);

		registerChatStats(
				Kills,
				Deaths,
				KDRatio,
				BlankLine,
				Assists,
				DamageTaken,
				DamageDealt,
				BlankLine,
				new ChatStatData("kit", "Kit", true)
		);
	}
	
	public SoloSuperSmash(ArcadeManager manager, Kit[] kits, GameType type)
	{
		super(manager, kits, type, new String[]
				{
				"Each player has 4 respawns",
				"Attack to restore hunger!",
				"Last player alive wins!"
					});
		
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
		_players.setDisplayName(C.cYellowB + "Players");
	}

	@Override
	@EventHandler
	public void ScoreboardUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
			return;

		//Wipe Last
		Scoreboard.reset();

		boolean minimise = false;
		
		if (GetPlayers(false).size() > 14)
		{
			minimise = true;
			
			Scoreboard.writeNewLine();
			Scoreboard.write(C.cGreenB + "Players Alive");
			Scoreboard.write(GetPlayers(true).size() + " ");

			Scoreboard.writeNewLine();
			Scoreboard.write(C.cRedB + "Players Dead");
			Scoreboard.write((GetPlayers(false).size() - GetPlayers(true).size()) + "  ");
		}
		else
		{
			Scoreboard.writeNewLine();

			Scoreboard.writeGroup(GetPlayers(minimise), player ->
			{
				int lives = getLives(player);

				return Pair.create(getLiveColour(lives) + player.getName(), lives);
			}, true);
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
