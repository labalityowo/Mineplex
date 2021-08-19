package mineplex.game.nano.game.games.quick;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilAlg;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.common.util.UtilTextMiddle;
import mineplex.core.common.util.UtilTextTop;
import mineplex.core.common.util.UtilTime;
import mineplex.core.lifetimes.Lifetime;
import mineplex.core.lifetimes.Lifetimed;
import mineplex.core.lifetimes.ListenerComponent;
import mineplex.core.lifetimes.SimpleLifetime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.nano.game.components.Disposable;
import mineplex.game.nano.game.event.PlayerDeathOutEvent;
import mineplex.game.nano.game.event.PlayerStateChangeEvent;

public abstract class Challenge extends ListenerComponent implements Lifetimed, Disposable
{

	private final SimpleLifetime _lifetime = new SimpleLifetime();

	protected final Quick _game;
	private final ChallengeType _challengeType;
	protected final ChallengeWinConditions _winConditions;

	protected List<Player> _players;
	protected long _startTime;
	protected long _timeout = TimeUnit.SECONDS.toMillis(10);
	private int _startingPlayers;
	private boolean _playerWon;

	protected boolean _pvp;

	public Challenge(Quick game, ChallengeType challengeType)
	{
		_game = game;
		_challengeType = challengeType;
		_winConditions = new ChallengeWinConditions();
		_lifetime.register(this);
	}

	public abstract void challengeSelect();

	public void start()
	{
		_startTime = System.currentTimeMillis();
		_players = _game.getAlivePlayers();
		_startingPlayers = _players.size();
		_lifetime.start();

		_players.forEach(player -> _game.respawnPlayer(player, _game.getPlayersTeam()));

		challengeSelect();

		UtilTextMiddle.display("", C.cYellow + _challengeType.getDescription(), 0, 40, 10, UtilServer.getPlayers());
		_game.announce("", null);
		_game.announce(" " + C.cYellowB + "Challenge! " + C.Reset + _challengeType.getDescription(), null);
		_game.announce("", null);
	}

	public void end()
	{
		disable();

		for (Entity entity : _game.getMineplexWorld().getWorld().getEntities())
		{
			if (entity instanceof Player)
			{
				continue;
			}

			entity.remove();
		}

		completeRemaining();

		_players.clear();
		_lifetime.end();
	}

	protected void completeRemaining()
	{
		for (Player player : _game.getAlivePlayers())
		{
			if (_winConditions.isTimeoutWin())
			{
				completePlayer(player, true);
			}
			else
			{
				failPlayer(player, true);
			}
		}
	}

	public void completePlayer(Player player, boolean out)
	{
		if (_players.remove(player))
		{
			UtilTextMiddle.display("", C.cGreen + "You completed the challenge!", 0, 30, 10, player);
			player.sendMessage(F.main(_game.getManager().getName(), "You completed the challenge."));
			_game.incrementScore(player, 1);

			_game.addStat(player, "QuickWins", 1, false, false);

			if (out)
			{
				_game.onOut(player, true);
			}

			player.playSound(player.getLocation(), Sound.VILLAGER_YES, 1, 1);

			if (!_playerWon && _winConditions.isTimeoutAfterFirst())
			{
				_playerWon = true;

				_game.announce(F.main(_game.getManager().getName(), "A player has completed the challenge. " + F.time(_winConditions.getTimeout() + " Second" + (_winConditions.getTimeout() == 1 ? "" : "s")) + " left."), Sound.NOTE_STICKS);
				_game.getManager().runSyncLater(() ->
				{
					if (isRunning())
					{
						end();
					}
				}, _winConditions.getTimeout() * 20);
			}
		}
	}

	public void failPlayer(Player player, boolean out)
	{
		if (_players.remove(player))
		{
			UtilTextMiddle.display("", C.cRed + "You failed the challenge!", 0, 30, 10, player);
			player.sendMessage(F.main(_game.getManager().getName(), "You failed the challenge."));

			if (out)
			{
				_game.onOut(player, false);
			}

			player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1, 1);
		}
	}

	public boolean isParticipating(Player player)
	{
		return _players.contains(player);
	}

	protected boolean isRunning()
	{
		return _lifetime.isActive() && _game.isLive();
	}

	protected boolean inArena(Block block)
	{
		return inArena(block.getLocation().add(0.5, 0.5, 0.5));
	}

	protected boolean inArena(Location location)
	{
		return UtilAlg.inBoundingBox(location, _game.getRedPoints().get(0), _game.getRedPoints().get(1));
	}

	@EventHandler
	public void update(UpdateEvent event)
	{
		if (event.getType() == UpdateType.SEC)
		{
			UtilTextBottom.display(C.cYellow + _challengeType.getDescription(), UtilServer.getPlayers());
		}
		else if (event.getType() == UpdateType.FASTER)
		{
			if (_players.isEmpty() || UtilTime.elapsed(_startTime, _timeout) || (_winConditions.isLastOne() && _players.size() == 1) || (_winConditions.isLastThree() && _players.size() <= getPlayersToEnd()))
			{
				end();
			}
			else
			{
				long diff = System.currentTimeMillis() - _startTime;

				UtilTextTop.displayProgress(C.mTime + UtilTime.MakeStr(_timeout - diff), 1 - (double) diff / _timeout, UtilServer.getPlayers());
			}
		}
	}

	@EventHandler
	public void playerOut(PlayerDeathOutEvent event)
	{
		Player player = event.getPlayer();

		event.setCancelled(true);
		event.setShouldRespawn(false);
		failPlayer(player, true);
	}

	@EventHandler
	public void playerOut(PlayerStateChangeEvent event)
	{
		if (!event.isAlive())
		{
			_players.remove(event.getPlayer());
		}
	}

	private int getPlayersToEnd()
	{
		return _startingPlayers > 5 ? 3 : 1;
	}

	boolean isPvp()
	{
		return _pvp;
	}

	@Override
	public Lifetime getLifetime()
	{
		return _lifetime;
	}
}
