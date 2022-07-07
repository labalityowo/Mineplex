package mineplex.game.nano.game.components.currency;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import mineplex.core.ReflectivelyCreateMiniPlugin;
import mineplex.core.account.CoreClient;
import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.achievement.Achievement;
import mineplex.core.boosters.Booster;
import mineplex.core.command.CommandBase;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.C;
import mineplex.core.portal.events.GenericServerTransferEvent;
import mineplex.core.portal.events.ServerTransferEvent;
import mineplex.core.stats.StatsManager;
import mineplex.core.titles.tracks.TrackManager;
import mineplex.core.titles.tracks.standard.GemCollectorTrack;
import mineplex.game.nano.GameManager;
import mineplex.game.nano.NanoManager;
import mineplex.game.nano.game.Game.GameState;
import mineplex.game.nano.game.event.GameStateChangeEvent;

@ReflectivelyCreateMiniPlugin
public class GameCurrencyManager extends GameManager implements CurrencyComponent
{

	public enum Perm implements Permission
	{
		SHARD_MULT_1,
		SHARD_MULT_2,
		SHARD_MULT_3,
		SHARD_MULT_4,
		SHARD_MULT_5,
		REWARDS_COMMAND
	}

	private final Map<Player, GameSessionData> _sessionData;

	private GameCurrencyManager()
	{
		super("Game Currency");

		_sessionData = new HashMap<>();

		generatePermissions();
	}

	private void generatePermissions()
	{
		PermissionGroup.ULTRA.setPermission(Perm.SHARD_MULT_1, true, true);
		PermissionGroup.HERO.setPermission(Perm.SHARD_MULT_2, true, true);
		PermissionGroup.LEGEND.setPermission(Perm.SHARD_MULT_3, true, true);
		PermissionGroup.TITAN.setPermission(Perm.SHARD_MULT_4, true, true);
		PermissionGroup.ETERNAL.setPermission(Perm.SHARD_MULT_5, true, true);
		PermissionGroup.PLAYER.setPermission(Perm.REWARDS_COMMAND, true, true);
	}

	@Override
	public void addCommands()
	{
		addCommand(new CommandBase<GameCurrencyManager>(this, Perm.REWARDS_COMMAND, "rewards")
		{
			@Override
			public void Execute(Player caller, String[] args)
			{
				informRewards(caller, false);
			}
		});
	}

	@EventHandler
	public void playerJoin(PlayerJoinEvent event)
	{
		_sessionData.put(event.getPlayer(), new GameSessionData());
	}

	@EventHandler
	public void gameEnd(GameStateChangeEvent event)
	{
		if (event.getState() != GameState.End)
		{
			return;
		}

		for (Player player : event.getGame().getAllPlayers())
		{
			GameSessionData data = _sessionData.get(player);

			if (data != null)
			{
				data.Games++;

				if (data.Games == 100)
				{
					event.getGame().addStat(player, "PlayInARow", 1, true, false);
				}
			}
		}
	}

	@Override
	public void addGems(Player player, int amount)
	{
		GameSessionData data = _sessionData.get(player);

		if (data != null)
		{
			data.Gems += amount;
		}
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void serverTransfer(ServerTransferEvent event)
	{
		informRewards(event.getPlayer(), true);
	}

	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
	public void serverTransfer(GenericServerTransferEvent event)
	{
		informRewards(event.getPlayer(), true);
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void playerQuit(PlayerQuitEvent event)
	{
		informRewards(event.getPlayer(), true);
	}

	private void informRewards(Player player, boolean reward)
	{
		GameSessionData data = reward ? _sessionData.remove(player) : _sessionData.get(player);

		if (data == null || data.Games == 0)
		{
			return;
		}

		StatsManager statsManager = _manager.getStatsManager();
		CoreClient client = _manager.getClientManager().Get(player);
		String gameName = NanoManager.getGameDisplay().getName();

		int gems = data.Gems;
		int shards = gems;
		int exp = gems * 6;

		// Gem Hunter Bonus
		int gemHunter = _manager.getAchievementManager().get(player, Achievement.GLOBAL_GEM_HUNTER).getLevel();

		if (gemHunter > 0)
		{
			gems += (int) (gems * (gemHunter * 0.25D));
		}

		// Shard Amplifier Bonus
		Booster booster = _manager.getBoosterManager().getActiveBooster();

		if (booster != null)
		{
			shards *= booster.getMultiplier();
		}

		// Shard Rank Bonus
		double shardMultiplier = 1;

		for (Perm shardMultPerm : Perm.values())
		{
			if (client.hasPermission(shardMultPerm))
			{
				shardMultiplier += 0.5;
			}
		}

		shards *= shardMultiplier;

		// Award
		if (reward)
		{
			if (_manager.getServerGroup().getRewardGems())
			{
				_manager.getDonationManager().rewardCurrencyUntilSuccess(GlobalCurrency.GEM, player, "Earned " + gameName, gems, null);
				_manager.getDonationManager().rewardCurrencyUntilSuccess(GlobalCurrency.TREASURE_SHARD, player, "Earned", shards, null);
			}

			if (_manager.getServerGroup().getRewardStats())
			{
				int gamesPlayed = (int) Math.floor(data.Games / 5D);

				statsManager.incrementStat(player, "Global.GemsEarned", gems);
				statsManager.incrementStat(player, gameName + ".GemsEarned", gems);
				statsManager.incrementStat(player, "Global.ExpEarned", exp);
				statsManager.incrementStat(player, gameName + ".ExpEarned", exp);
				statsManager.incrementStat(player, "Global.GamesPlayed", gamesPlayed);
				require(TrackManager.class).getTrack(GemCollectorTrack.class).earnedGems(player, gems);
			}
		}

		// Inform
		player.sendMessage(NanoManager.getHeaderFooter());
		player.sendMessage(C.Bold + "Game Rewards");
		player.sendMessage("");
		player.sendMessage("  " + C.cGray + "Games Played: " + C.cYellow + data.Games);

		if (_manager.getServerGroup().getRewardGems())
		{
			player.sendMessage("  " + C.cGray + "+" + C.cGreen + gems + C.cGray + " Gems");
			player.sendMessage("  " + C.cGray + "+" + C.cAqua + shards + C.cGray + " Shards");
		}

		if (_manager.getServerGroup().getRewardStats())
		{
			player.sendMessage("  " + C.cGray + "+" + C.cYellow + exp + C.cGray + " Experience");
		}

		player.sendMessage("");
		player.sendMessage(NanoManager.getHeaderFooter());		player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 0.4F);
	}

	private class GameSessionData
	{
		int Gems;
		int Games;
	}
}
