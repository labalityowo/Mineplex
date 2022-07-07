package nautilus.game.arcade.managers.voting.types;

import java.util.List;
import java.util.stream.Collectors;

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

public class MapVote extends Vote<VotableMap>
{

	private final GameType _gameType;

	public MapVote(ArcadeManager manager, GameType gameType, List<String> maps)
	{
		super(manager, "Map", new ItemBuilder(Material.PAPER)
				.setTitle(C.cGreenB + "Vote for the next Map")
				.addLore("", "Click to vote on the next", "map that's going to be played!")
				.build(), maps.stream()
				.map(VotableMap::new)
				.collect(Collectors.toList()));

		_gameType = gameType;
	}

	@Override
	public void vote(Player player, VotableMap value)
	{
		super.vote(player, value);

		player.closeInventory();
	}

	@Override
	public void onEnd()
	{
		Bukkit.broadcastMessage(C.cGreenB + getWinner().getDisplayName() + C.cWhiteB + " won the vote!");

		for (Player player : UtilServer.getPlayersCollection())
		{
			player.playSound(player.getLocation(), Sound.NOTE_PIANO, 1, 1);
		}
	}

	@Override
	public List<VoteRating> getMapRatings()
	{
		VotableMap winner = getWinner();
		int ratingWin = getValues().size() - 1, ratingLose = -1;

		return getValues().stream()
				.map(votableMap -> new VoteRating(_gameType.getGameId(), votableMap.getDisplayName(), winner.equals(votableMap) ? ratingWin : ratingLose))
				.collect(Collectors.toList());
	}

	@Override
	public boolean isBlocking()
	{
		return false;
	}
}
