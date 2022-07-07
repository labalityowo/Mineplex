package mineplex.game.nano.game.components.prepare;

import java.util.concurrent.TimeUnit;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.scheduler.BukkitRunnable;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTextTop;
import mineplex.core.common.util.UtilTime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.nano.NanoManager;
import mineplex.game.nano.game.Game;
import mineplex.game.nano.game.Game.GameState;
import mineplex.game.nano.game.GameComponent;
import mineplex.game.nano.game.event.GameStateChangeEvent;

public class GamePrepareComponent extends GameComponent<Game>
{

	private long _prepareTime = TimeUnit.SECONDS.toMillis(10);
	private boolean _prepareFreeze = true;

	private boolean _colourTick;

	public GamePrepareComponent(Game game)
	{
		super(game, GameState.Prepare);
	}

	public GamePrepareComponent setPrepareTime(long prepareTime)
	{
		_prepareTime = prepareTime;
		return this;
	}

	public GamePrepareComponent setPrepareFreeze(boolean prepareFreeze)
	{
		_prepareFreeze = prepareFreeze;
		return this;
	}

	@Override
	public void disable()
	{

	}

	@EventHandler
	public void prepare(GameStateChangeEvent event)
	{
		if (event.getState() != GameState.Prepare)
		{
			return;
		}

		announceGame();

		getGame().getManager().runSyncTimer(new BukkitRunnable()
		{
			int ticks = 0;
			int messageIndex = -1;

			@Override
			public void run()
			{
				if (getGame().getState() != GameState.Prepare)
				{
					cancel();
					return;
				}

				long diff = System.currentTimeMillis() - getGame().getStateTime();

				if (diff > _prepareTime)
				{
					cancel();

					if (getGame().getAllPlayers().size() > 1)
					{
						announceStart();
						getGame().setState(GameState.Live);
					}
					else
					{
						getGame().setState(GameState.Dead);
					}
				}
				else
				{
					Player[] players = UtilServer.getPlayers();

					if (ticks % 20 == 0)
					{
						for (Player player : players)
						{
							player.playSound(player.getLocation(), Sound.NOTE_STICKS, 1, 1);
						}
					}

					if (ticks % 40 == 0 && messageIndex < getGame().getDescription().length)
					{
						String title = C.cYellowB + getGame().getGameType().getName();

						if (messageIndex == -1)
						{
							UtilTextMiddle.display(title, null, 0, 45, 0, players);
						}
						else
						{
							UtilTextMiddle.display(title, getGame().getDescription()[messageIndex], 0, 45, 0, players);
						}

						messageIndex++;
					}

					UtilTextBottom.displayProgress("Game Start", (double) diff / _prepareTime, UtilTime.MakeStr(Math.max(0, _prepareTime - diff)), players);
					ticks++;
				}
			}
		}, 0, 1);
	}

	@EventHandler(ignoreCancelled = true)
	public void playerMove(PlayerMoveEvent event)
	{
		if (!_prepareFreeze || !getGame().isAlive(event.getPlayer()))
		{
			return;
		}

		Location from = event.getFrom(), to = event.getTo();

		if (from.getX() == to.getX() && from.getZ() == to.getZ())
		{
			return;
		}

		event.setTo(from);
	}

	@EventHandler
	public void updateBossBar(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FASTER)
		{
			return;
		}

		UtilTextTop.display((_colourTick ? C.cGoldB : C.cWhiteB) + "MINEPLEX.COM" + C.cGrayB + " - " + (_colourTick ? C.cWhiteB : C.cGoldB) + "NANO GAMES", UtilServer.getPlayers());
		_colourTick = !_colourTick;
	}

	private void announceGame()
	{
		String[] description = getGame().getDescription();

		for (Player player : UtilServer.getPlayersCollection())
		{
			player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 1);
		}

		for (int i = 0; i < 6 - description.length; i++)
		{
			Bukkit.broadcastMessage("");
		}

		Bukkit.broadcastMessage(NanoManager.getHeaderFooter());

		Bukkit.broadcastMessage(C.cGreen + "Game - " + C.cYellowB + getGame().getGameType().getName());
		Bukkit.broadcastMessage("");

		for (String line : description)
		{
			Bukkit.broadcastMessage(C.cWhite + "  " + line);
		}

		Bukkit.broadcastMessage("");
		Bukkit.broadcastMessage(getGame().getMineplexWorld().getFormattedName());

		Bukkit.broadcastMessage(NanoManager.getHeaderFooter());

		getGame().getManager().getChat().setChatSilence(_prepareTime, false);
	}

	private void announceStart()
	{
		Player[] players = UtilServer.getPlayers();
		UtilTextBottom.display(C.cGreenB + "Start!", players);

		for (Player player : players)
		{
			player.playSound(player.getLocation(), Sound.NOTE_PLING, 1, 1);
		}
	}
}
