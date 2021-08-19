package mineplex.game.nano.game.games.quick.challenges;

import java.util.concurrent.TimeUnit;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import mineplex.core.common.util.UtilTime;
import mineplex.game.nano.game.games.quick.Challenge;
import mineplex.game.nano.game.games.quick.ChallengeType;
import mineplex.game.nano.game.games.quick.Quick;

public class ChallengeStandStill extends Challenge
{

	public ChallengeStandStill(Quick game)
	{
		super(game, ChallengeType.STAND_STILL);

		_timeout = TimeUnit.SECONDS.toMillis(8);
		_winConditions.setTimeoutWin(true);
	}

	@Override
	public void challengeSelect()
	{
	}

	@Override
	public void disable()
	{
	}

	@EventHandler
	public void playerMove(PlayerMoveEvent event)
	{
		if (!UtilTime.elapsed(_startTime, 1000))
		{
			return;
		}

		Player player = event.getPlayer();

		if (!isParticipating(player))
		{
			return;
		}

		Location from = event.getFrom(), to = event.getTo();

		if (from.getBlockX() == to.getBlockX() && from.getBlockZ() == to.getBlockZ())
		{
			return;
		}

		failPlayer(player, true);
	}
}
