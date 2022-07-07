package mineplex.game.nano.game.games.quick.challenges;

import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerToggleSneakEvent;

import mineplex.game.nano.game.games.quick.Challenge;
import mineplex.game.nano.game.games.quick.ChallengeType;
import mineplex.game.nano.game.games.quick.Quick;

public class ChallengeCrouch extends Challenge
{

	public ChallengeCrouch(Quick game)
	{
		super(game, ChallengeType.CROUCH);

		_timeout = 1500;
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
	public void playerSneak(PlayerToggleSneakEvent event)
	{
		if (event.isSneaking())
		{
			completePlayer(event.getPlayer(), false);
		}
	}
}
