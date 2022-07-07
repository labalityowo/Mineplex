package nautilus.game.arcade.gui.privateServer.page;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.shop.item.ShopItem;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.gui.privateServer.PrivateServerShop;

public class GameVotingPage extends BasePage
{

	private int _page;

	public GameVotingPage(ArcadeManager plugin, PrivateServerShop shop, Player player)
	{
		super(plugin, shop, "Game Voting Menu", player, 54);
		buildPage();
	}

	@Override
	protected void buildPage()
	{
		boolean host = getPlugin().GetGameHostManager().isHost(getPlayer());

		if (host)
		{
			addBackButton(4);
			addStartVoteButton(0);
			addEndVoteButton(8);
			addPickHighestButton(1);
		}
		else
		{
			addCloseButton(4);
		}

		int gameSlot = 9;
		int gameIndex = _page * GAMES_PER_PAGE;
		List<GameType> types = new ArrayList<>(_manager.getGames());
		types = types.subList(gameIndex, Math.min(types.size(), (_page + 1) * GAMES_PER_PAGE));

		for (GameType type : types)
		{
			addGameButton(gameSlot++, type);
		}

		if (_page > 0)
		{
			addButton(45, PREVIOUS_PAGE, (player, clickType) ->
			{
				_page--;
				refresh();
			});
		}

		if (types.size() == GAMES_PER_PAGE)
		{
			addButton(53, NEXT_PAGE, (player, clickType) ->
			{
				_page++;
				refresh();
			});
		}
	}

	private void addPickHighestButton(int slot)
	{
		ShopItem item = new ShopItem(Material.DIAMOND_BLOCK, (byte)0, "§b§lSelect Highest Voted Game", new String[]{"§7Game: §e" + getHighestVoted().getName()}, 1, false, false);
		addButton(slot, item, (player, clickType) ->
		{
			if (!_manager.isHost(player)) //Double Check...
				return;

			_manager.setVoteInProgress(false);
			getPlugin().GetGame().setGame(getHighestVoted(), player, true);
		});
	}

	private GameType getHighestVoted()
	{
		GameType highest = GameType.Skywars;
		int highestVotes = getVotes(GameType.Skywars);
		for (GameType cur : _manager.getVotes().values())
		{
			if (getVotes(cur) > highestVotes)
			{
				highestVotes = getVotes(cur);
				highest = cur;
			}
		}
		return highest;
	}

	private int getVotes(GameType type)
	{
		int i = 0;
		for (GameType cur : _manager.getVotes().values())
		{
			if (cur.equals(type))
				i++;
		}
		return i;
	}

	private void addCloseButton(int slot)
	{
		ShopItem item = new ShopItem(Material.BED, (byte)0, "§cClose Menu", new String[]{}, 1, false, false);
		addButton(slot, item, (player, clickType) -> player.closeInventory());
	}

	private void addEndVoteButton(int slot)
	{
		ShopItem item = new ShopItem(Material.REDSTONE_BLOCK, (byte)0, "§c§lEnd Vote", new String[]{}, 1, false, false);
		addButton(slot, item, (player, clickType) ->
		{
			if (!_manager.isHost(player)) //Double Check...
				return;

			getPlugin().GetGameHostManager().setVoteInProgress(false);
			for (Player p : UtilServer.getPlayers())
			{
				UtilPlayer.message(p, F.main("Vote", "The vote has ended!"));
			}
			refresh();
		});
	}

	private void addStartVoteButton(int slot)
	{
		String warning = "§c§lWARNING: §fThis will reset current votes!";
		ShopItem item = new ShopItem(Material.EMERALD_BLOCK, (byte)0, "Start Vote", new String[]{warning}, 1, false, false);
		addButton(slot, item, (player, clickType) ->
		{
			if (!_manager.isHost(player)) //Double Check...
				return;

			getPlugin().GetGameHostManager().setVoteInProgress(true);
			getPlugin().GetGameHostManager().getVotes().clear();
			for (Player p : UtilServer.getPlayers())
			{
				UtilPlayer.message(p, F.main("Vote", "A vote has started! Use " + F.skill("/vote") + " to vote."));
				p.playSound(p.getLocation(), Sound.NOTE_BASS, 1F, 1F);
			}
			refresh();
		});
	}

	private void addGameButton(int slot, final GameType type)
	{
		String click = "§7Click to vote for this Game Type";
		int votes = 0;
		for (GameType cur : getPlugin().GetGameHostManager().getVotes().values())
		{
			if (cur.equals(type))
				votes++;
		}
		String curVotes = "§7Votes: §e" + votes;

		ShopItem item = new ShopItem(type.GetMaterial(), type.GetMaterialData(), type.getName(), new String[]{click, curVotes}, 1, false, false);

		if (votes >= 1)
		{
			addButton(slot, item, (player, clickType) ->
			{
				getPlugin().GetGameHostManager().getVotes().put(player.getName(), type);
				refresh();
			});
		}
		else
		{
			addButtonFakeCount(slot, item, (player, clickType) ->
			{
				getPlugin().GetGameHostManager().getVotes().put(player.getName(), type);
				refresh();
			}, Math.max(1, votes));
		}
	}
}
