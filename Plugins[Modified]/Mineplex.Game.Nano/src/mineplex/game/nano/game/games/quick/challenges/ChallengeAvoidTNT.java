package mineplex.game.nano.game.games.quick.challenges;

import org.bukkit.Location;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityExplodeEvent;

import mineplex.core.common.util.UtilAlg;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.nano.game.games.quick.Challenge;
import mineplex.game.nano.game.games.quick.ChallengeType;
import mineplex.game.nano.game.games.quick.Quick;

public class ChallengeAvoidTNT extends Challenge
{

	public ChallengeAvoidTNT(Quick game)
	{
		super(game, ChallengeType.AVOID_TNT);

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
	public void updateTNT(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SEC)
		{
			return;
		}

		Location location = UtilAlg.getRandomLocation(_game.getCenter(), _game.getArenaSize(), 0, _game.getArenaSize());
		TNTPrimed tnt = location.getWorld().spawn(location.add(0, 7, 0), TNTPrimed.class);
		tnt.setFuseTicks(40);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void entityExplode(EntityExplodeEvent event)
	{
		event.blockList().clear();
	}
}
