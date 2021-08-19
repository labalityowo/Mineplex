package nautilus.game.arcade.managers.voting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import mineplex.core.common.util.UtilAlg;

import nautilus.game.arcade.ArcadeManager;

public abstract class Vote<T extends Voteable>
{

	public static class VoteData<T extends Voteable>
	{
		private final T _value;
		private final double _rankWeight;

		VoteData(T value, double rankWeight)
		{
			_value = value;
			_rankWeight = rankWeight;
		}

		public T getValue()
		{
			return _value;
		}
	}

	private static final int VOTE_TIME = 20;

	private final ArcadeManager _manager;
	private final String _name;
	private final ItemStack _itemStack;
	private final List<T> _values;
	private final Map<Player, VoteData<T>> _voteData;

	private int _timer;
	private T _winnerCache;

	public Vote(ArcadeManager manager, String name, ItemStack itemStack, List<T> values)
	{
		_manager = manager;
		_name = name;
		_itemStack = itemStack;
		_values = values;
		_voteData = new ConcurrentHashMap<>();
		_timer = VOTE_TIME;
	}

	public abstract void onEnd();

	public abstract List<VoteRating> getMapRatings();

	public abstract boolean isBlocking();

	public void vote(Player player, T value)
	{
		if (value == null)
		{
			_voteData.remove(player);
		}
		else
		{
			_voteData.put(player, new VoteData<>(value, _manager.GetGameCreationManager().getVotingManager().getVoteWeight(player)));
		}

		_winnerCache = null;
	}

	public void removeVote(Player player)
	{
		vote(player, null);
		player.closeInventory();
	}

	public T getWinner()
	{
		if (_winnerCache != null)
		{
			return _winnerCache;
		}

		Map<T, Double> results = new HashMap<>(_values.size());

		_voteData.values().forEach(voteData -> results.put(voteData.getValue(), results.getOrDefault(voteData.getValue(), 0D) + voteData._rankWeight));

		List<T> winners = new ArrayList<>(_values.size());
		double winningVotes = -1;

		for (Entry<T, Double> entry : results.entrySet())
		{
			T value = entry.getKey();
			double votes = entry.getValue();

			if (votes == winningVotes)
			{
				winners.add(value);
			}
			else if (votes > winningVotes)
			{
				winners.clear();
				winners.add(value);
				winningVotes = votes;
			}
		}

		if (winners.size() == 1)
		{
			_winnerCache = winners.get(0);
		}
		else if (winners.isEmpty())
		{
			_winnerCache =  UtilAlg.Random(_values);
		}
		else
		{
			_winnerCache = UtilAlg.Random(winners);
		}

		return _winnerCache;
	}

	public VoteData<T> getPlayerVote(Player player)
	{
		return _voteData.get(player);
	}

	public ArcadeManager getManager()
	{
		return _manager;
	}

	public String getName()
	{
		return _name;
	}

	public ItemStack getItemStack()
	{
		return _itemStack;
	}

	public List<T> getValues()
	{
		return _values;
	}

	public boolean decrementTimer()
	{
		int voted = _voteData.size();

		return voted >= _manager.GetPlayerFull() && voted == _manager.getValidPlayersForGameStart().size() || --_timer == 0;
	}

	public int getTimer()
	{
		return _timer;
	}
}
