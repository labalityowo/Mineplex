package nautilus.game.arcade.game.games.mineware.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import nautilus.game.arcade.game.games.mineware.challenge.Challenge;

/**
 * An event that triggers when a challenge starts.
 */
public class ChallengeStartEvent extends Event
{
	private Challenge _startedChallenge;
	private static final HandlerList _handlers = new HandlerList();

	public ChallengeStartEvent(Challenge startedChallenge)
	{
		_startedChallenge = startedChallenge;
	}

	public Challenge getStartedChallenge()
	{
		return _startedChallenge;
	}

	public static HandlerList getHandlerList()
	{
		return _handlers;
	}

	@Override
	public HandlerList getHandlers()
	{
		return getHandlerList();
	}
}
