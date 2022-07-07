package mineplex.core.poll;

import mineplex.core.common.util.NautHashMap;

/**
 * Created by Shaun on 8/17/2014.
 */
public class PlayerPollData
{
	// Cooldown for next poll if the player doesn't answer
	private static long POLL_COOLDOWN = 180000;

	private NautHashMap<Integer, Integer> _pollAnswers;
	private long _nextPollTime;

	public boolean Loaded;
	
	public PlayerPollData()
	{
		_pollAnswers = new NautHashMap<Integer, Integer>();
	}

	public boolean hasAnswered(Poll poll)
	{
		return _pollAnswers.containsKey(poll.getId());
	}

	public void addAnswer(int pollId, int answer)
	{
		_pollAnswers.put(pollId, answer);
	}

	public void updatePollCooldown()
	{
		_nextPollTime = System.currentTimeMillis() + POLL_COOLDOWN;
	}

	public void setPollCooldown(long cooldown)
	{
		_nextPollTime = System.currentTimeMillis() + cooldown;
	}

	public boolean shouldPoll()
	{
		return Loaded && System.currentTimeMillis() > _nextPollTime;
	}
}