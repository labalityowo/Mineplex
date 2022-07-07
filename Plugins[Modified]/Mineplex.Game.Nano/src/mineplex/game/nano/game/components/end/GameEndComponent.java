package mineplex.game.nano.game.components.end;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.FireworkEffect.Type;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilFirework;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextTop;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.nano.NanoManager;
import mineplex.game.nano.game.Game;
import mineplex.game.nano.game.Game.GameState;
import mineplex.game.nano.game.GameComponent;
import mineplex.game.nano.game.GamePlacements;
import mineplex.game.nano.game.components.team.GameTeam;
import mineplex.game.nano.game.event.GameStateChangeEvent;
import mineplex.game.nano.game.event.GameTimeoutEvent;
import mineplex.game.nano.game.event.PlayerStateChangeEvent;

public class GameEndComponent extends GameComponent<Game>
{

	public enum AnnouncementType
	{
		SOLO,
		TEAM
	}

	private AnnouncementType _announcementType = AnnouncementType.SOLO;
	private long _winEffectTime = TimeUnit.SECONDS.toMillis(4);
	private long _timeout = TimeUnit.MINUTES.toMillis(4);

	public GameEndComponent(Game game)
	{
		super(game, GameState.Live, GameState.End);
	}

	public GameEndComponent setAnnouncementType(AnnouncementType announcementType)
	{
		_announcementType = announcementType;
		return this;
	}

	public GameEndComponent setWinEffectTime(long winEffectTime)
	{
		_winEffectTime = winEffectTime;
		return this;
	}

	public GameEndComponent setTimeout(long timeout)
	{
		_timeout = timeout;
		return this;
	}

	@Override
	public void disable()
	{

	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void playerOut(PlayerStateChangeEvent event)
	{
		if (getGame().isLive() && getGame().endGame())
		{
			getGame().setState(GameState.End);
		}
	}

	@EventHandler
	public void updateTimeout(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST || !getGame().isLive() || _timeout < 0)
		{
			return;
		}

		if (UtilTime.elapsed(getGame().getStateTime(), _timeout))
		{
			UtilServer.CallEvent(new GameTimeoutEvent(getGame()));
			getGame().setState(GameState.End);
		}
		else
		{
			long diff = System.currentTimeMillis() - getGame().getStateTime();
			UtilTextTop.displayProgress(C.mTime + UtilTime.MakeStr(_timeout - diff), 1 - (double) diff / _timeout, UtilServer.getPlayers());
		}
	}

	@EventHandler
	public void end(GameStateChangeEvent event)
	{
		if (event.getState() != GameState.End)
		{
			return;
		}

		announceEnd();

		List<Player> winners = getGame().getGamePlacements() == null ? Collections.emptyList() : getGame().getGamePlacements().getWinners();

		getGame().getManager().runSyncTimer(new BukkitRunnable()
		{
			@Override
			public void run()
			{
				if (getGame().getState() != GameState.End || UtilTime.elapsed(getGame().getStateTime(), _winEffectTime))
				{
					cancel();
					getGame().setState(GameState.Dead);
					return;
				}

				Player player = UtilAlg.Random(winners);

				if (player != null)
				{
					GameTeam team = getGame().getTeam(player);

					if (team == null)
					{
						return;
					}

					Location location = UtilAlg.getRandomLocation(player.getLocation().add(0, 20, 0), 10, 3, 10);

					UtilFirework.playFirework(location, FireworkEffect.builder()
							.with(Type.BALL_LARGE)
							.withColor(team.getColour())
							.withFade(Color.WHITE)
							.build());
				}
			}
		}, 0, 10);
	}

	private void announceEnd()
	{
		for (Player player : UtilServer.getPlayersCollection())
		{
			player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 1);
		}

		for (Player player : getGame().getAllPlayers())
		{
			getGame().addGems(player, 10);
		}

		Bukkit.broadcastMessage(NanoManager.getHeaderFooter());

		Bukkit.broadcastMessage(C.cGreen + "Game - " + C.cYellowB + getGame().getGameType().getName());
		Bukkit.broadcastMessage("");

		switch (_announcementType)
		{
			case SOLO:
				announceSolo();
				break;
			case TEAM:
				announceTeam();
				break;
		}

		Bukkit.broadcastMessage("");
		Bukkit.broadcastMessage(getGame().getMineplexWorld().getFormattedName());

		Bukkit.broadcastMessage(NanoManager.getHeaderFooter());
	}

	private void announceSolo()
	{
		GamePlacements placements = getGame().getGamePlacements();

		if (placements == null || !placements.hasPlacements())
		{
			announceNobody();
			return;
		}

		List<Player> first = placements.getPlayersAtPlace(0), second = placements.getPlayersAtPlace(1), third = placements.getPlayersAtPlace(2);

		Bukkit.broadcastMessage(C.cRedB + "1st Place " + C.cWhite + buildPlacementLine(first));
		first.forEach(player -> getGame().addGems(player, 20));

		if (second != null)
		{
			Bukkit.broadcastMessage(C.cGoldB + "2nd Place " + C.cWhite + buildPlacementLine(second));
			second.forEach(player -> getGame().addGems(player, 15));
		}

		if (third != null)
		{
			Bukkit.broadcastMessage(C.cYellowB + "3rd Place " + C.cWhite + buildPlacementLine(third));
			third.forEach(player -> getGame().addGems(player, 10));
		}
	}

	private String buildPlacementLine(List<Player> places)
	{
		if (places == null || places.isEmpty())
		{
			return null;
		}

		StringBuilder builder = new StringBuilder();

		for (int i = 0; i < places.size() - 1; i++)
		{
			builder.append(places.get(i).getName()).append(", ");
		}

		return builder
				.append(places.get(places.size() - 1).getName())
				.toString();
	}

	private void announceTeam()
	{
		GameTeam winner = getGame().getWinningTeam();

		Bukkit.broadcastMessage("");

		if (winner == null)
		{
			announceNobody();
		}
		else
		{
			Bukkit.broadcastMessage(" " + winner.getChatColour() + C.Bold + winner.getName() + " won the game!");

			winner.getAllPlayers().forEach(player -> getGame().addGems(player, 20));
		}

		Bukkit.broadcastMessage("");
	}

	private void announceNobody()
	{
		Bukkit.broadcastMessage(C.cWhiteB + " Nobody won the game...");
	}
}
