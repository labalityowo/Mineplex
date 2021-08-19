package nautilus.game.arcade.game.games.moba;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilTime;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.moba.boss.wither.WitherBoss;
import nautilus.game.arcade.game.games.moba.general.HotJoiningManager;
import nautilus.game.arcade.game.games.moba.prepare.PrepareManager;
import nautilus.game.arcade.game.games.moba.prepare.PrepareSelection;
import nautilus.game.arcade.game.modules.CustomScoreboardModule;
import nautilus.game.arcade.scoreboard.GameScoreboard;
import org.bukkit.ChatColor;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class MobaClassic extends Moba
{

	private static final String[] DESCRIPTION = {
			"Select your Role and Hero.",
			"Kill the enemy Team!",
			"Capture the Beacons to earn gold to spent in the shop!",
			"Destroy the enemy's Towers and Wither!"
	};
	private static final long PREPARE_TIME = TimeUnit.MINUTES.toMillis(1);

	public MobaClassic(ArcadeManager manager)
	{
		super(manager, GameType.MOBA, DESCRIPTION);

		PrepareAutoAnnounce = false;
		PrepareFreeze = false;
		PrepareTime = PREPARE_TIME;
		DeathOut = false;
		DeathSpectateSecs = 12;
		HungerSet = 20;
		DamageFall = false;

		// Pregame managers
		registerManager(new PrepareManager(this));
		registerManager(new PrepareSelection(this));

		// Hot joining
		registerManager(new HotJoiningManager(this));

//		new GameStatisticsModule()
//				.register(this);

		new CustomScoreboardModule()
				.setSidebar((player, scoreboard) ->
				{
					GameState state = GetState();

					switch (state)
					{
						case Prepare:
							writePrepare(player, scoreboard);
							break;
						case Live:
							writeLive(player, scoreboard);
							break;
					}
				})
				.setPrefix((perspective, subject) ->
				{
					if (!IsAlive(subject))
					{
						return C.cGray;
					}

					GameTeam team = GetTeam(subject);

					return team.GetColor().toString();
				})
				.setSuffix((perspective, subject) ->
				{
					GameState state = GetState();
					GameTeam perspectiveTeam = GetTeam(perspective);
					GameTeam subjectTeam = GetTeam(subject);

					if (!IsAlive(subject) || perspectiveTeam == null || subjectTeam == null)
					{
						return "";
					}

					MobaPlayer mobaPlayer = getMobaData(subject);
					String suffix;

					if (state == GameState.Prepare && !perspectiveTeam.equals(subjectTeam))
					{
						suffix = C.cYellow + " Unknown";
					}
					else if (mobaPlayer == null || mobaPlayer.getKit() == null)
					{
						suffix = C.cYellow + " Selecting";
					}
					else
					{
						suffix = mobaPlayer.getRole().getChatColor() + " [" + mobaPlayer.getKit().GetName() + "]";
					}

					return suffix + C.Reset;
				})
				.setUnderNameObjective(C.cRed + "❤")
				.setUnderName((perspective, subject) ->
						(int) (Math.ceil(subject.getHealth() / 2D)))
				.register(this);
	}

	private void writePrepare(Player player, GameScoreboard scoreboard)
	{
		MobaPlayer mobaPlayer = getMobaData(player);

		scoreboard.writeNewLine();

		scoreboard.write(C.cYellowB + "Hero Selection");
		scoreboard.write(UtilTime.MakeStr(GetStateTime() + PREPARE_TIME - System.currentTimeMillis()));

		scoreboard.writeNewLine();

		scoreboard.write(C.cYellowB + "Hero");
		scoreboard.write((mobaPlayer == null || mobaPlayer.getKit() == null) ? "Unselected " : mobaPlayer.getKit().GetName() + " (" + mobaPlayer.getRole().getName() + ")");

		scoreboard.writeNewLine();

		scoreboard.write(C.cYellowB + "Players");
		int kits = 0;

		for (MobaPlayer otherMobaPlayer : getMobaData())
		{
			if (otherMobaPlayer.getKit() != null)
			{
				kits++;
			}
		}

		scoreboard.write(kits + "/" + GetPlayers(true).size());

		scoreboard.writeNewLine();
	}

	private void writeLive(Player player, GameScoreboard scoreboard)
	{
		GameTeam team = GetTeam(player);
		boolean alive = IsAlive(player);

		scoreboard.writeNewLine();

		// Towers
		GameTeam red = GetTeam(ChatColor.RED);
		GameTeam blue = GetTeam(ChatColor.AQUA);
		String redTitle;
		String blueTitle;

		if (alive)
		{
			boolean playerRed = team.equals(red);
			redTitle = playerRed ? "Your Team" : "Enemy Team";
			blueTitle = playerRed ? "Enemy Team" : "Your Team";
		}
		else
		{
			redTitle = "Red Team";
			blueTitle = "Blue Team";
		}

		scoreboard.write(red.GetColor() + C.Bold + redTitle);
		scoreboard.write("Base: " + _tower.getDisplayString(red) + _boss.getWitherDisplayString(red));

		scoreboard.writeNewLine();

		scoreboard.write(blue.GetColor() + C.Bold + blueTitle);
		scoreboard.write("Base: " + _tower.getDisplayString(blue) + _boss.getWitherDisplayString(blue));

		scoreboard.writeNewLine();

		scoreboard.write(C.cGreenB + "Beacons");
		scoreboard.write(_capturePoint.getDisplayString());

		scoreboard.writeNewLine();

		// Gold
		scoreboard.write(C.cGoldB + "Your Gold");
		if (alive)
		{
			int gold = _goldManager.getGold(player);

			scoreboard.write(String.valueOf(gold));
		}
		else
		{
			scoreboard.write("None");
		}

		scoreboard.writeNewLine();

		scoreboard.write(C.cYellowB + "Time");
		scoreboard.write(UtilTime.MakeStr(System.currentTimeMillis() - GetStateTime()));
	}

	@Override
	public void EndCheck()
	{
		if (!IsLive())
		{
			return;
		}

		// Only one team online check
		List<GameTeam> teamsWithPlayers = new ArrayList<>(GetTeamList().size());

		for (GameTeam team : GetTeamList())
		{
			if (team.GetPlayers(true).isEmpty())
			{
				continue;
			}

			teamsWithPlayers.add(team);
		}

		if (teamsWithPlayers.size() == 1)
		{
			AnnounceEnd(teamsWithPlayers.get(0));
			SetState(GameState.End);
			return;
		}

		// Wither Dead check
		for (GameTeam team : GetTeamList())
		{
			WitherBoss boss = _boss.getWitherBoss(team);
			LivingEntity entity = boss.getEntity();

			// Dead Wither
			if (entity == null || !entity.isValid() || entity.isDead())
			{
				// Get the other team
				for (GameTeam otherTeam : GetTeamList())
				{
					for (Player player : otherTeam.GetPlayers(true))
					{
						AddGems(player, 10, "Participation", false, false);
					}

					if (team.equals(otherTeam))
					{
						continue;
					}

					for (Player player : otherTeam.GetPlayers(true))
					{
						MobaPlayer mobaPlayer = getMobaData(player);

						AddStat(player, mobaPlayer.getRole().getName() + ".Wins", 1, true, false);
						AddGems(player, 20, "Winning", false, false);
					}

					AnnounceEnd(otherTeam);
					SetState(GameState.End);
					return;
				}
			}
		}
	}

}
