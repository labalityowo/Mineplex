package nautilus.game.arcade.managers.voting;

import java.util.List;
import java.util.function.Consumer;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

import mineplex.core.account.CoreClient;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTextBottom;
import mineplex.core.game.GameDisplay;
import mineplex.core.lifetimes.Lifetime;
import mineplex.core.lifetimes.Lifetimed;
import mineplex.core.lifetimes.ListenerComponent;
import mineplex.core.lifetimes.SimpleLifetime;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.managers.voting.command.MapRatingsCommand;
import nautilus.game.arcade.managers.voting.event.VoteStartEvent;
import nautilus.game.arcade.managers.voting.ui.VotingPage;
import nautilus.game.arcade.managers.voting.ui.VotingShop;

public class VotingManager extends ListenerComponent implements Lifetimed
{

	public enum Perm implements Permission
	{
		MAP_RATINGS_COMMAND
	}

	public enum WeightPerm implements Permission
	{
		VOTING_WEIGHT_10,
		VOTING_WEIGHT_20,
		VOTING_WEIGHT_30,
		VOTING_WEIGHT_40,
		VOTING_WEIGHT_50,
	}

	public static final int GAMES_TO_VOTE_ON = 3;
	public static final int MAPS_TO_VOTE_ON = 5;

	private final ArcadeManager _manager;
	private final VotingShop _shop;
	private final VotingRepository _repository;
	private final SimpleLifetime _lifetime;

	private Vote<?> _currentVote, _finishedVote;
	private boolean _colourTick, _waitingForPlayers;

	public VotingManager(ArcadeManager manager)
	{
		_manager = manager;
		_shop = new VotingShop(manager, manager.GetClients(), manager.GetDonation());
		_repository = new VotingRepository();
		_lifetime = new SimpleLifetime();

		generatePermissions();
		manager.addCommand(new MapRatingsCommand(manager));
	}

	private void generatePermissions()
	{
		PermissionGroup.ADMIN.setPermission(Perm.MAP_RATINGS_COMMAND, true, true);
		PermissionGroup.ULTRA.setPermission(WeightPerm.VOTING_WEIGHT_10, true, true);
		PermissionGroup.HERO.setPermission(WeightPerm.VOTING_WEIGHT_20, true, true);
		PermissionGroup.LEGEND.setPermission(WeightPerm.VOTING_WEIGHT_30, true, true);
		PermissionGroup.TITAN.setPermission(WeightPerm.VOTING_WEIGHT_40, true, true);
		PermissionGroup.ETERNAL.setPermission(WeightPerm.VOTING_WEIGHT_50, true, true);
	}

	public void callVote(Vote vote)
	{
		_currentVote = vote;

		for (Player player : UtilServer.getPlayersCollection())
		{
			player.getInventory().setItem(0, _currentVote.getItemStack());
		}

		_waitingForPlayers = false;
		_manager.GetLobby().displayVoting(vote);

		activate();
		_lifetime.start();

		UtilServer.CallEvent(new VoteStartEvent(vote));
	}

	@Override
	public void deactivate()
	{
		if (!_lifetime.isActive())
		{
			return;
		}

		super.deactivate();

		if (_currentVote != null)
		{
			ItemStack remove = _currentVote.getItemStack();

			for (Player player : UtilServer.getPlayersCollection())
			{
				player.getInventory().remove(remove);
				UtilPlayer.closeInventoryIfOpen(player);
			}

			if (_manager.GetGame() != null)
			{
				_manager.GetLobby().displayGame(_manager.GetGame());
			}

			_manager.GetLobby().displayWaiting(false);

			_currentVote.onEnd();

			_currentVote = null;
			_finishedVote = null;
		}

		_lifetime.end();
	}

	@EventHandler
	public void playerJoin(PlayerJoinEvent event)
	{
		if (!isVoteInProgress())
		{
			return;
		}

		Player player = event.getPlayer();

		player.getInventory().setItem(0, _currentVote.getItemStack());
	}

	@EventHandler
	public void playerQuit(PlayerQuitEvent event)
	{
		if (!isVoteInProgress())
		{
			return;
		}

		Player player = event.getPlayer();

		_currentVote.removeVote(player);
	}

	@EventHandler
	public void playerInteract(PlayerInteractEvent event)
	{
		if (event.getAction() == Action.PHYSICAL || !isVoteInProgress())
		{
			return;
		}

		Player player = event.getPlayer();
		ItemStack itemStack = player.getItemInHand();

		if (itemStack == null || !itemStack.equals(_currentVote.getItemStack()))
		{
			return;
		}

		openVotePage(player, _currentVote);
	}

	public void openVotePage(Player player, Vote<?> vote)
	{
		_shop.openPageForPlayer(player, new VotingPage<>(_manager, _shop, player, vote));
	}

	@EventHandler
	public void informPlayers(UpdateEvent event)
	{
		if (!isVoteInProgress() || _finishedVote != null)
		{
			return;
		}

		if (event.getType() == UpdateType.TWOSEC)
		{
			String primary = C.cGold, secondary = C.cYellow;

			if (_colourTick)
			{
				String temp = primary;
				primary = secondary;
				secondary = temp;
			}

			UtilTextBottom.display(primary + "Click the " + secondary + C.Bold + "Paper" + primary + " to pick the next " + secondary + C.Bold + _currentVote.getName(), UtilServer.getPlayers());
			_colourTick = !_colourTick;
		}
		else if (event.getType() == UpdateType.SEC)
		{
			if (_currentVote.decrementTimer())
			{
				_finishedVote = _currentVote;
			}

			_manager.GetLobby().displayVotingTime(_currentVote);
		}
	}

	public void updateMapRatings()
	{
		List<VoteRating> ratings = _finishedVote.getMapRatings();

		_manager.runAsync(() -> _repository.updateRatings(ratings));
	}

	public void getMapRatings(Consumer<List<VoteRating>> callback, GameDisplay gameDisplay)
	{
		_manager.runAsync(() -> callback.accept(_repository.getRatings(gameDisplay.getGameId())));
	}

	public double getVoteWeight(Player player)
	{
		CoreClient client = _manager.GetClients().Get(player);
		double weight = 1;

		for (WeightPerm perm : WeightPerm.values())
		{
			if (client.hasPermission(perm))
			{
				weight += 0.1;
			}
		}

		return weight;
	}

	public Vote<?> getFinishedVote()
	{
		return _finishedVote;
	}

	public Vote<?> getCurrentVote()
	{
		return _currentVote;
	}

	public boolean isVoteInProgress()
	{
		return _currentVote != null;
	}

	public boolean isVoteBlocking()
	{
		return isVoteInProgress() && getCurrentVote().isBlocking();
	}

	public boolean canStartVote()
	{
		if (_manager.getValidPlayersForGameStart().size() < _manager.GetPlayerMin())
		{
			if (!_waitingForPlayers)
			{
				_waitingForPlayers = true;
				_manager.GetLobby().displayWaiting(true);
			}
			return false;
		}

		return true;
	}

	@Override
	public Lifetime getLifetime()
	{
		return _lifetime;
	}
}
