package nautilus.game.arcade.game.games.battleroyale;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.C;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilMath;
import mineplex.core.common.util.UtilTime;
import mineplex.core.server.util.TransactionResponse;
import mineplex.core.treasure.types.TreasureType;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.GameType;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.GameTeam;
import nautilus.game.arcade.game.games.moba.kit.KitPlayer;
import nautilus.game.arcade.game.modules.CustomScoreboardModule;
import nautilus.game.arcade.kit.Kit;
import nautilus.game.arcade.scoreboard.GameScoreboard;

public class BattleRoyaleSolo extends BattleRoyale
{

	private static final String[] DESCRIPTION = {
			"Battle Royale!"
	};

	private GameTeam _players;

	// Scoreboard data
	private final String _playersAliveTitle = C.cYellowB + "Players";
	private String _playersAlive;
	private final String _statsTitle = C.cYellowB + "Stats";
	private String _supplyDropTitle = C.cGoldB + "Supply Drop";
	private String _supplyDropLocation;
	private String _supplyDropState;
	private final String _borderTitle = C.cRedB + "World Border";
	private String _borderCenter;
	private String _borderSize;
	private boolean _showBorderCenter;

	public BattleRoyaleSolo(ArcadeManager manager)
	{
		super(manager, GameType.BattleRoyale, new Kit[]{new KitPlayer(manager)}, DESCRIPTION);

		new CustomScoreboardModule()
				.setSidebar((player, scoreboard) ->
				{
					switch (GetState())
					{
						case Prepare:
							writePrepare(player, scoreboard);
							break;
						case Live:
							writeLive(player, scoreboard);
							break;
					}
				})
				.setPrefix((perspective, subject) ->
				{
					if (!IsAlive(subject))
					{
						return C.cGray;
					}

					return perspective.equals(subject) ? C.cGreen : C.cRed;
				})
				.register(this);
	}

	@EventHandler
	public void customTeamGeneration(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Recruit)
		{
			return;
		}

		_players = GetTeamList().get(0);
		_players.SetColor(ChatColor.YELLOW);
		_players.SetName("Players");
	}

	// LOW so that this is run before the scoreboards are updated
	@EventHandler(priority = EventPriority.LOW)
	public void scoreboardDataUpdate(UpdateEvent event)
	{
		if (event.getType() != UpdateType.FAST || _border == null)
		{
			return;
		}

		// Due to many players being in this game and the fact that the scoreboard module scales O(n^2)
		// we can optimise this by storing global variables that all players would see on their scoreboard
		// regardless of their state.
		_playersAlive = GetPlayers(true).size() + " Alive";

		if (_supplyDrop != null)
		{
			Location location = _supplyDrop.getDropLocation();
			_supplyDropLocation = "(" + location.getBlockX() + ", " + location.getBlockZ() + ")";
			_supplyDropState = _supplyDrop.getScoreboardString();
		}
		else
		{
			_supplyDropLocation = "";
			_supplyDropState = UtilTime.MakeStr(_lastSupplyDrop + BattleRoyale.SUPPLY_DROP_TIME - System.currentTimeMillis());
		}

		int size = (int) _border.getSize();
		Location center = _border.getCenter();

		if (size < 1000 && !_showBorderCenter)
		{
			_showBorderCenter = true;
			Announce(C.cRedB + "The Center Of The Border Is Now Visible!");
		}

		if (_showBorderCenter)
		{
			_borderCenter = "(" + center.getBlockX() + ", " + center.getBlockZ() + ")";
		}
		else
		{
			_borderCenter = "Center Unknown";
		}

		_borderSize = size + " Blocks Wide";
	}

	public void writePrepare(Player player, GameScoreboard scoreboard)
	{
		scoreboard.writeNewLine();

		scoreboard.write(_playersAliveTitle);
		scoreboard.write(_playersAlive);

		scoreboard.writeNewLine();
	}

	public void writeLive(Player player, GameScoreboard scoreboard)
	{
		BattleRoyalePlayer royalePlayer = _playerData.get(player);

		scoreboard.writeNewLine();

		scoreboard.write(_playersAliveTitle);
		scoreboard.write(_playersAlive);

		scoreboard.writeNewLine();

		if (royalePlayer != null)
		{
			scoreboard.write(_statsTitle);
			scoreboard.write("Kills: " + C.cGreen + royalePlayer.getKills());
			scoreboard.write("Assists: " + C.cGreen + royalePlayer.getAssists());

			scoreboard.writeNewLine();
		}

		scoreboard.write(_supplyDropTitle);
		if (_supplyDrop != null)
		{
			scoreboard.write(_supplyDropLocation);
		}
		if (_supplyDropState != null)
		{
			scoreboard.write(_supplyDropState);
		}
		else if (_supplyDrop != null && IsAlive(player))
		{
			int dist = (int) UtilMath.offset2d(_supplyDrop.getDropLocation(), player.getLocation());

			scoreboard.write(dist + " Blocks Away");
		}

		scoreboard.writeNewLine();

		scoreboard.write(_borderTitle);
		scoreboard.write(_borderCenter);
		scoreboard.write(_borderSize);
	}

	@Override
	public void EndCheck()
	{
		if (!IsLive())
		{
			return;
		}

		List<Player> alive = GetPlayers(true);

		if (alive.size() <= 1)
		{
			alive.forEach(this::awardTimeGems);

			List<Player> places = _players.GetPlacements(true);

			AnnounceEnd(places);

			if (places.size() >= 1)
//			{
//				Player player = places.get(0);
//				long wins = Manager.GetStatsManager().Get(player).getStat("Battle Royale.Wins");
//
//				if (wins > 1)
//				{
//					Manager.GetDonation().purchaseUnknownSalesPackage(player, TreasureType.MYTHICAL.getItemName(), GlobalCurrency.GEM, 0, false, data ->
//					{
//						if (data == TransactionResponse.Success)
//						{
//							Manager.getInventoryManager().addItemToInventory(success ->
//							{
//								if (success)
//								{
//									player.sendMessage(F.main("Game", "Unlocked 1 " + C.cAqua + "Mythical Chest" + C.mBody + "."));
//								}
//								else
//								{
//									player.sendMessage(F.main("Game", "Failed to give you your Mythical Chest, you should take a screenshot of this and make a support ticket!"));
//									player.sendMessage(C.cGray + "Error Verification Code: " + C.cGreen + new StringBuilder(player.getUniqueId().toString().split("-")[1]).reverse().toString());
//								}
//							}, player, TreasureType.MYTHICAL.getItemName(), 1);
//						}
//						else
//						{
//							player.sendMessage(F.main("Game", "Failed to give you your Mythical Chest, you should take a screenshot of this and make a support ticket!"));
//							player.sendMessage(C.cGray + "Error Verification Code: " + C.cGreen + new StringBuilder(player.getUniqueId().toString().split("-")[1]).reverse().toString());
//						}
//					});
//				}
//				else
//				{
//					Manager.GetDonation().purchaseUnknownSalesPackage(player, TreasureType.FREEDOM.getItemName(), GlobalCurrency.GEM, 0, false, data ->
//					{
//						if (data == TransactionResponse.Success)
//						{
//							Manager.getInventoryManager().addItemToInventory(success ->
//							{
//								if (success)
//								{
//									player.sendMessage(F.main("Game", "Unlocked 1 " + C.cRed + "Freedom Chest" + C.mBody + "."));
//								}
//								else
//								{
//									player.sendMessage(F.main("Game", "Failed to give you your Freedom Chest, you should take a screenshot of this and make a support ticket!"));
//									player.sendMessage(C.cGray + "Error Verification Code: " + C.cGreen + new StringBuilder(player.getUniqueId().toString().split("-")[1]).reverse().toString());
//								}
//							}, player, TreasureType.FREEDOM.getItemName(), 1);
//						}
//						else
//						{
//							player.sendMessage(F.main("Game", "Failed to give you your Freedom Chest, you should take a screenshot of this and make a support ticket!"));
//							player.sendMessage(C.cGray + "Error Verification Code: " + C.cGreen + new StringBuilder(player.getUniqueId().toString().split("-")[1]).reverse().toString());
//						}
//					});
//				}

				AddGems(places.get(0), 20, "1st Place", false, false);
			//}
			if (places.size() >= 2)
			{
				AddGems(places.get(1), 15, "2nd Place", false, false);
			}
			if (places.size() >= 3)
			{
				AddGems(places.get(2), 10, "3rd Place", false, false);
			}

			_border.setSize(10000);
			SetState(GameState.End);
		}
	}

	@Override
	public List<Player> getWinners()
	{
		if (GetState().ordinal() >= GameState.End.ordinal())
		{
			List<Player> places = _players.GetPlacements(true);

			if (places.isEmpty() || !places.get(0).isOnline())
			{
				return new ArrayList<>(0);
			}
			else
			{
				return Collections.singletonList(places.get(0));
			}
		}
		else
		{
			return null;
		}
	}

	@Override
	public List<Player> getLosers()
	{
		List<Player> winners = getWinners();

		if (winners == null)
		{
			return null;
		}

		List<Player> losers = _players.GetPlayers(false);
		losers.removeAll(winners);

		return losers;
	}
}
