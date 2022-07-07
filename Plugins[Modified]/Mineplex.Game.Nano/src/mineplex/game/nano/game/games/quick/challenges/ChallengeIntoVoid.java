package mineplex.game.nano.game.games.quick.challenges;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import mineplex.game.nano.game.games.quick.Challenge;
import mineplex.game.nano.game.games.quick.ChallengeType;
import mineplex.game.nano.game.games.quick.Quick;
import mineplex.minecraft.game.core.damage.CustomDamageEvent;

public class ChallengeIntoVoid extends Challenge
{

	public ChallengeIntoVoid(Quick game)
	{
		super(game, ChallengeType.INTO_VOID);

		_winConditions.setTimeoutAfterFirst(true, 1);
	}

	@Override
	public void challengeSelect()
	{
	}

	@Override
	public void disable()
	{
	}

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	public void damage(CustomDamageEvent event)
	{
		Player damagee = event.GetDamageePlayer();

		if (damagee != null)
		{
			event.SetCancelled("Into Void");
			completePlayer(damagee, true);
		}
	}
}
