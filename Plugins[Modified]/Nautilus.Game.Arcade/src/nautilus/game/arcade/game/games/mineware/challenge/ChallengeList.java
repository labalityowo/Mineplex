package nautilus.game.arcade.game.games.mineware.challenge;

import java.util.ArrayList;
import java.util.List;

import mineplex.core.common.util.UtilMath;

public class ChallengeList
{
	private List<Challenge> _challenges = new ArrayList<>();
	private List<Challenge> _played = new ArrayList<>();
	private Challenge _restricted;

	public Challenge random()
	{
		if (_restricted == null)
		{
			if (_played.size() == _challenges.size())
			{
				_played.clear();
				return UtilMath.randomElement(_challenges);
			}
			else
			{
				Challenge challenge = UtilMath.randomElement(_challenges);

				while (_played.contains(challenge))
				{
					challenge = UtilMath.randomElement(_challenges);
				}

				return challenge;
			}
		}
		else
		{
			return _restricted;
		}
	}

	public void add(Challenge... challenges)
	{
		for (Challenge challenge : challenges)
		{
			_challenges.add(challenge);
		}
	}

	public void addPlayed(Challenge challenge)
	{
		_played.add(challenge);
	}

	public boolean restrict(String name)
	{
		for (Challenge challenge : _challenges)
		{
			if (challenge.getName().contains(name))
			{
				_restricted = challenge;
				return true;
			}
		}
		
		return false;
	}
	
	public void resetPlayed()
	{
		_played.clear();
	}

	public void unrestrict()
	{
		_restricted = null;
	}

	public int size()
	{
		return _challenges.size();
	}
	
	public int played()
	{
		return _played.size();
	}
}
