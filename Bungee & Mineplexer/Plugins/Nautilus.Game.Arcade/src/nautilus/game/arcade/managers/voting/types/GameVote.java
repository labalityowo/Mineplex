package nautilus.game.arcade.managers.voting.types;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilServer;
import mineplex.core.itemstack.ItemBuilder;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.managers.voting.Vote;
import nautilus.game.arcade.managers.voting.VoteRating;

public class GameVote extends Vote<GameType>
{

	private final Map<GameType, MapVote> _mapVotes;

	private MapVote _winningMapVote;

	public GameVote(ArcadeManager manager, List<GameType> values, Map<GameType, List<String>> gameMaps)
	{
		super(manager, "Game", new ItemBuilder(Material.PAPER)
				.setTitle(C.cGreenB + "Vote for the next Game")
				.addLore("", "Click to vote on the next", "game that's going to be played!")
				.build(), values);

		if (gameMaps.isEmpty())
		{
			_mapVotes = Collections.emptyMap();
		}
		else
		{
			_mapVotes = new HashMap<>();

			gameMaps.forEach((gameType, maps) ->_mapVotes.put(gameType, new MapVote(manager, gameType, maps)));
		}
	}

	@Override
	public void vote(Player player, GameType value)
	{
		super.vote(player, value);

		if (_mapVotes.isEmpty())
		{
			player.closeInventory();
		}
		else if (value != null)
		{
			MapVote mapVote = _mapVotes.get(value);

			if (mapVote != null)
			{
				getManager().GetGameCreationManager().getVotingManager().openVotePage(player, mapVote);
			}
			else
			{
				player.closeInventory();
			}
		}
	}

	@Override
	public GameType getWinner()
	{
		GameType winner = super.getWinner();
		MapVote mapVote = _mapVotes.get(winner);

		if (mapVote != null)
		{
			_winningMapVote = mapVote;
		}

		return winner;
	}

	@Override
	public void onEnd()
	{
		// true when /game set has been used during a vote
		if (getManager().GetGameCreationManager().getNextGameType() != null)
		{
			return;
		}

		String message = C.cGreenB + getWinner().getName() + C.cWhiteB;

		if (_winningMapVote != null)
		{
			message += " and map " + C.cGreenB + _winningMapVote.getWinner().getDisplayName() + C.cWhiteB;
		}

		Bukkit.broadcastMessage(message + " won the vote!");

		for (Player player : UtilServer.getPlayersCollection())
		{
			player.playSound(player.getLocation(), Sound.NOTE_PIANO, 1, 1);
		}
	}

	@Override
	public List<VoteRating> getMapRatings()
	{
		return _winningMapVote.getMapRatings();
	}

	@Override
	public boolean isBlocking()
	{
		return true;
	}

	public MapVote getWinningMapVote()
	{
		return _winningMapVote;
	}
}
