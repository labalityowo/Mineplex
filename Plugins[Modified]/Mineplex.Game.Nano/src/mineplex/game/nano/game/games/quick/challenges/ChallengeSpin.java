package mineplex.game.nano.game.games.quick.challenges;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.nano.game.games.quick.Challenge;
import mineplex.game.nano.game.games.quick.ChallengeType;
import mineplex.game.nano.game.games.quick.Quick;

public class ChallengeSpin extends Challenge
{

	private final Map<Player, Float> _yaw;

	public ChallengeSpin(Quick game)
	{
		super(game, ChallengeType.SPIN);

		_yaw = new HashMap<>();

		_timeout = TimeUnit.SECONDS.toMillis(5);
	}

	@Override
	public void challengeSelect()
	{
		for (Player player : _players)
		{
			_yaw.put(player, player.getLocation().getYaw());
		}
	}

	@Override
	public void disable()
	{
		_yaw.clear();
	}

	@EventHandler
	public void updateSpin(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST)
		{
			return;
		}

		_yaw.entrySet().forEach(entry ->
		{
			Player player = entry.getKey();
			float newYaw = player.getLocation().getYaw();
			float lastYaw = entry.getValue();

			if (Math.abs(newYaw - lastYaw) > 180)
			{
				completePlayer(player, false);
			}
			else
			{
				entry.setValue(newYaw);
			}
		});
	}
}
