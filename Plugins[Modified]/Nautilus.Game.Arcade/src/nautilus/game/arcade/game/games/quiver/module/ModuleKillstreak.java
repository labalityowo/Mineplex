package nautilus.game.arcade.game.games.quiver.module;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Sound;
import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.quiver.QuiverTeamBase;
import nautilus.game.arcade.game.games.quiver.QuiverTeamBase.GemAwardReason;

public class ModuleKillstreak extends QuiverTeamModule
{

	private static final long KILLSTREAK_TIME_PERIOD = 1500;
	private static final long TEAM_KILL_MINIMUM_DELAY = 20000;
	private static final int TEAM_KILL_MINIMUM_PLAYERS = 6;

	private Map<UUID, Integer> _killstreakAmount = new HashMap<>();
	private Map<UUID, Long> _killstreamLast = new HashMap<>();
	private long _lastTeamKill;

	public ModuleKillstreak(QuiverTeamBase base)
	{
		super(base);
	}

	@Override
	public void setup()
	{
	}

	@Override
	public void update(UpdateType updateType)
	{
		if (updateType != UpdateType.FAST)
		{
			return;
		}

		if (getBase().GetPlayers(true).size() >= TEAM_KILL_MINIMUM_PLAYERS)
		{
			for (GameTeam gameTeam : getBase().GetTeamList())
			{
				if (!UtilTime.elapsed(_lastTeamKill, TEAM_KILL_MINIMUM_DELAY))
				{
					break;
				}

				boolean gameTeamKill = true;

				for (Player player : gameTeam.GetPlayers(false))
				{
					if (!UtilPlayer.isSpectator(player))
					{
						gameTeamKill = false;
					}
				}

				if (gameTeamKill)
				{
					String message = gameTeam.GetColor() + C.Bold + "DOMINATION";

					UtilTextMiddle.display(message, "", 10, 30, 10);
					UtilServer.broadcast(message);

					_lastTeamKill = System.currentTimeMillis();
				}
			}
		}

		for (Player player : getBase().GetPlayers(true))
		{
			if (_killstreamLast.containsKey(player.getUniqueId()) && _killstreakAmount.containsKey(player.getUniqueId()))
			{
				long lastKill = _killstreamLast.get(player.getUniqueId());
				int kills = _killstreakAmount.get(player.getUniqueId());

				if (UtilTime.elapsed(lastKill, KILLSTREAK_TIME_PERIOD))
				{
					if (kills > 1)
					{
						String name = null;

						switch (kills)
						{
						case 3:
							name = "TRIPLE";
							break;
						case 4:
							name = "QUADRA";
							break;
						case 5:
							name = "PENTA";
							break;
						case 6:
							name = "HEXA";
							break;
						case 7:
							name = "SEPTA";
							break;
						case 8:
							name = "OCTA";
							break;
						case 9:
							name = "NONA";
							break;
						case 10:
							name = "DECA";
							break;
						}

						if (kills >= 3)
						{
							getBase().AddStat(player, "SteadyHands", 1, false, false);
						}

						if (name != null)
						{
							for (Player other : UtilServer.getPlayers())
							{
								other.playSound(other.getLocation(), Sound.ENDERDRAGON_GROWL, 1F + kills, 1f + kills);
							}

							getBase().AddGems(player, getBase().getGems(GemAwardReason.KILLSTEAK) * kills, name + " Killstreak", true, true);
							UtilServer.broadcast(C.cGreenB + player.getName() + C.cWhite + " got a " + C.cGreenB + name + " KILL" + C.cWhite + "!");
						}
					}

					_killstreakAmount.put(player.getUniqueId(), 0);
				}
			}
		}
	}

	@Override
	public void finish()
	{
	}

	public Map<UUID, Integer> getKillstreakAmount()
	{
		return _killstreakAmount;
	}

	public Map<UUID, Long> getKillstreakTime()
	{
		return _killstreamLast;
	}

}
