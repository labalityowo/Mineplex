package nautilus.game.arcade.managers;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import mineplex.core.account.permissions.Permission;
import mineplex.core.account.permissions.PermissionGroup;
import mineplex.core.achievement.Achievement;
import mineplex.core.boosters.Booster;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.F;
import mineplex.core.common.util.UtilPlayer;
import mineplex.core.common.util.UtilServer;
import mineplex.core.common.util.UtilTime;
import mineplex.core.titles.tracks.standard.GemCollectorTrack;
import mineplex.minecraft.game.core.combat.CombatComponent;
import mineplex.minecraft.game.core.combat.event.CombatDeathEvent;
import mineplex.serverdata.Utility;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.events.FirstBloodEvent;
import nautilus.game.arcade.events.GameStateChangeEvent;
import nautilus.game.arcade.game.Game;
import nautilus.game.arcade.game.Game.GameState;
import nautilus.game.arcade.game.GemData;

/**
 * This class is used to reward gems and shards at the end of a game, or when players leave the server.
 */
public class GameRewardManager implements Listener
{

	public enum Perm implements Permission
	{
		SHARD_MULT_1,
		SHARD_MULT_2,
		SHARD_MULT_3,
		SHARD_MULT_4,
		SHARD_MULT_5,
	}

	ArcadeManager Manager;

	boolean DoubleGem = false;
	boolean TimeReward = true;

	private final Map<Player, Integer> _baseShardsEarned;

	public GameRewardManager(ArcadeManager manager)
	{
		Manager = manager;

		_baseShardsEarned = new HashMap<>();
		UtilServer.RegisterEvents(this);

		generatePermissions();
	}

	private void generatePermissions()
	{
		PermissionGroup.ULTRA.setPermission(Perm.SHARD_MULT_1, true, true);
		PermissionGroup.HERO.setPermission(Perm.SHARD_MULT_2, true, true);
		PermissionGroup.LEGEND.setPermission(Perm.SHARD_MULT_3, true, true);
		PermissionGroup.TITAN.setPermission(Perm.SHARD_MULT_4, true, true);
		PermissionGroup.ETERNAL.setPermission(Perm.SHARD_MULT_5, true, true);
	}

	@EventHandler
	public void PlayerKillAward(CombatDeathEvent event)
	{
		if (!Manager.IsRewardGems())
			return;

		Game game = Manager.GetGame();
		if (game == null) return;

		Player killed = event.GetEvent().getEntity();

		if (event.GetLog().GetKiller() != null)
		{
			Player killer = UtilPlayer.searchExact(event.GetLog().GetKiller().getUniqueIdOfEntity());

			if (killer != null && !killer.equals(killed))
			{
				//Kill
				game.AddGems(killer, game.GetKillsGems(killer, killed, false), "Kills", true, true);

				//First Kill
				if (game.FirstKill)
				{
					game.AddGems(killer, game.FirstKillReward, "First Blood", false, false);

					Manager.getPluginManager().callEvent(new FirstBloodEvent(killer));

					game.FirstKill = false;

					game.Announce(F.main("Game", Manager.GetColor(killer) + killer.getName() + " drew first blood!"));
				}
			}
		}

		for (CombatComponent log : event.GetLog().GetAttackers())
		{
			if (event.GetLog().GetKiller() != null && log.equals(event.GetLog().GetKiller()))
				continue;

			Player assist = UtilPlayer.searchExact(log.GetName());

			//Assist
			if (assist != null)
				game.AddGems(assist, game.GetKillsGems(assist, killed, true), "Kill Assists", true, true);
		}
	}

	@EventHandler
	public void PlayerQuit(PlayerQuitEvent event)
	{
		if (!Manager.IsRewardGems())
			return;

		Game game = Manager.GetGame();
		if (game == null) return;

		GiveGems(game, event.getPlayer());
	}

	@EventHandler(priority = EventPriority.LOW)
	public void gameDead(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Dead)
		{
			return;
		}

		UtilServer.getPlayersCollection().forEach(player -> GiveGems(event.GetGame(), player));
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void gameDeadCleanup(GameStateChangeEvent event)
	{
		if (event.GetState() != GameState.Dead)
		{
			return;
		}

		_baseShardsEarned.clear();
	}

	private void GiveGems(Game game, Player player)
	{
		if (!Manager.IsRewardGems())
		{
			return;
		}

		double gameMult = game.GemMultiplier;
		Map<String, GemData> gems = game.GetGems(player);

		if (gems == null)
		{
			return;
		}

		final int baseGemsEarned;
		int gemsToReward;

		// Calculate the base gems earned in this game
		int gemsEarned = 0;

		for (GemData data : gems.values())
		{
			gemsEarned += (int) data.Gems;
		}

		if (gemsEarned <= 0)
		{
			gemsEarned = 1;
		}

		baseGemsEarned = (int) (gemsEarned * gameMult);
		gemsToReward = baseGemsEarned;

		// Award players shards equal to base gems, plus booster bonuses.
		final int baseShardsEarned = baseGemsEarned;
		int shardsToReward = baseShardsEarned;
		_baseShardsEarned.put(player, baseShardsEarned);

		// Gem Boooster
		Booster booster = Manager.getBoosterManager().getActiveBooster();
		if (game.GemBoosterEnabled && booster != null)
		{
			shardsToReward *= booster.getMultiplier();
		}

		// Gem Finder
		if (game.GemHunterEnabled && !game.CrownsEnabled)
		{
			int gemFinder = Manager.GetAchievement().get(player, Achievement.GLOBAL_GEM_HUNTER).getLevel();
			if (gemFinder > 0)
			{
				double factor = gemFinder * 0.25D;
				int gemFinderGems = (int) (baseGemsEarned * factor);
				gemsToReward += gemFinderGems;
				game.AddGems(player, gemFinderGems, "Gem Hunter " + gemFinder + " +" + (int) (factor * 100) + "%", false, false);
			}
		}

		// Time Reward
		if (TimeReward)
		{
			long timeOnline = Utility.currentTimeMillis() - Manager.GetClients().Get(player).getNetworkSessionLoginTime();

			double hoursOnline = timeOnline / 3600000d;

			if (hoursOnline < 24)
			{
				if (hoursOnline > 5)
					hoursOnline = 5;

				int timeGems = (int) (baseGemsEarned * (hoursOnline * 0.2D));
				gemsToReward += timeGems;
				game.AddGems(player, timeGems, "Online for " + UtilTime.MakeStr(timeGems), false, false);
			}
		}

		if (DoubleGem && game.GemDoubleEnabled && !game.CrownsEnabled)
		{
			gemsToReward += baseGemsEarned;
			game.AddGems(player, baseGemsEarned, "Double Gem Weekend", false, false);
		}

		int accountId = Manager.GetClients().getAccountId(player);

		double shardMult = 0;

		for (Perm shardMultPerm : Perm.values())
		{
			if (Manager.GetClients().Get(player).hasPermission(shardMultPerm))
			{
				shardMult += 0.5;
			}
		}

		if (shardMult > 0)
		{
			shardsToReward += shardsToReward * shardMult;
		}

		if (!game.CrownsEnabled)
		{
			Manager.GetDonation().rewardCurrency(GlobalCurrency.GEM, player, "Earned " + game.GetName(), gemsToReward);
		}
		else
		{
			Manager.GetDonation().rewardCrowns(gemsToReward, player);
		}
		if (accountId != -1)
		{
			Manager.GetDonation().rewardCurrencyUntilSuccess(GlobalCurrency.TREASURE_SHARD, player, "Earned", shardsToReward);
		}
		if (!game.CrownsEnabled)
		{
			Manager.getTrackManager().getTrack(GemCollectorTrack.class).earnedGems(player, gemsToReward);
		}

		//Stats
		if (!game.CrownsEnabled)
		{
			Manager.GetStatsManager().incrementStat(player, "Global.GemsEarned", gemsToReward);
			Manager.GetStatsManager().incrementStat(player, game.GetName() + ".GemsEarned", gemsToReward);
		}
		else
		{
			Manager.GetStatsManager().incrementStat(player, "Global.CrownsEarned", gemsToReward);
			Manager.GetStatsManager().incrementStat(player, game.GetName() + ".CrownsEarned", gemsToReward);
		}
	}

	public Integer getBaseShardsEarned(Player player)
	{
		return _baseShardsEarned.get(player);
	}
}
