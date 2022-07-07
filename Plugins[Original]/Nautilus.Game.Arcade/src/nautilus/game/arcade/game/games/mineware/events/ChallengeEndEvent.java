package nautilus.game.arcade.game.games.mineware.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import nautilus.game.arcade.game.games.mineware.challenge.Challenge;

/**
 * An event that triggers when a challenge ends (the game may end afterwards).
 */
public class ChallengeEndEvent extends Event
{
	private Challenge _endedChallenge;
	private static final HandlerList _handlers = new HandlerList();

	public ChallengeEndEvent(Challenge endedChallenge)
	{
		_endedChallenge = endedChallenge;
	}

	public Challenge getEndedChallenge()
	{
		return _endedChallenge;
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
