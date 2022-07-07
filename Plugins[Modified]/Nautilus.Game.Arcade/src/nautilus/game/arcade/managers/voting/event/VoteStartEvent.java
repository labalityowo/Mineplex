package nautilus.game.arcade.managers.voting.event;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import nautilus.game.arcade.managers.voting.Vote;

public class VoteStartEvent extends Event
{

	private static final HandlerList HANDLER_LIST = new HandlerList();

	public static HandlerList getHandlerList()
	{
		return HANDLER_LIST;
	}

	private final Vote<?> _vote;

	public VoteStartEvent(Vote<?> vote)
	{
		_vote = vote;
	}

	public Vote<?> getVote()
	{
		return _vote;
	}

	@Override
	public HandlerList getHandlers()
	{
		return HANDLER_LIST;
	}
}
