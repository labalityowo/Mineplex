package mineplex.game.nano.game.components.stats;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import mineplex.core.achievement.Achievement;
import mineplex.core.achievement.AchievementCategory;
import mineplex.core.achievement.AchievementData;
import mineplex.core.achievement.AchievementLog;
import mineplex.core.achievement.AchievementManager;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.util.C;
import mineplex.core.common.util.UtilServer;
import mineplex.core.donation.DonationManager;
import mineplex.core.stats.StatsManager;
import mineplex.core.task.TaskManager;
import mineplex.core.updater.UpdateType;
import mineplex.core.updater.event.UpdateEvent;
import mineplex.game.nano.game.Game;
import mineplex.game.nano.game.GameComponent;

public class GameStatsComponent extends GameComponent<Game> implements StatsComponent
{

	private final StatsManager _statsManager;
	private final TaskManager _taskManager;
	private final DonationManager _donationManager;

	private final Map<Player, Set<String>> _statsGiven;

	public GameStatsComponent(Game game)
	{
		super(game);

		_statsManager = game.getManager().getStatsManager();
		_taskManager = game.getManager().getTaskManager();
		_donationManager = game.getManager().getDonationManager();

		_statsGiven = new HashMap<>();
	}

	@Override
	public void disable()
	{

	}

	@Override
	public void addStat(Player player, String stat, int amount, boolean limitTo1, boolean global)
	{
		if (!getGame().getManager().getServerGroup().getRewardStats())
		{
			return;
		}

		Set<String> statsGiven = _statsGiven.computeIfAbsent(player, k -> new HashSet<>());

		if (global)
		{
			stat = AchievementCategory.GLOBAL.getFriendlyName() + "." + stat;
		}
		else
		{
			stat = AchievementCategory.NANO_GAMES.getFriendlyName() + "." + stat;
		}

		if (!statsGiven.add(stat) && limitTo1)
		{
			return;
		}

		_statsManager.incrementStat(player, stat, amount);
	}

	@EventHandler
	public void playerJoin(PlayerJoinEvent event)
	{
		getGame().getManager().getAchievementManager().clearLog(event.getPlayer());
	}

	/*
		Copied from AchievementSummaryComponent
		// TODO Replace and reinvent when reimplementing achievements
	 */

	@EventHandler
	public void updateAchievements(UpdateEvent event)
	{
		if (event.getType() != UpdateType.SLOW)
		{
			return;
		}

		AchievementManager manager = getGame().getManager().getAchievementManager();

		for (Player player : UtilServer.getPlayersCollection())
		{
			if (_statsManager.Get(player).isTemporary())
			{
				continue;
			}

			Map<Achievement, AchievementLog> logs = manager.getLog(player);

			if (logs == null)
			{
				continue;
			}

			logs.forEach((achievement, log) ->
			{
				AchievementData data = manager.get(player, achievement);

				if (log.LevelUp)
				{
					String nameLevel = getAchievementName(achievement, data);
					StringBuilder description = new StringBuilder();

					for (String line : achievement.getDesc())
					{
						description.append("\n").append(C.cDAqua).append(line);
					}

					int[] rewards = getRewards(achievement, data.getLevel());
					String title;
					String reward = getRewardsString(player, rewards);

					if (data.getLevel() >= achievement.getMaxLevel())
					{
						title = C.cPurpleB + "Achievement Get! ";

						if (!_taskManager.hasCompletedTask(player, achievement.getName()))
						{
							_taskManager.completedTask(success ->
							{
								if (success)
								{
									_donationManager.rewardCurrencyUntilSuccess(GlobalCurrency.GEM, player, achievement.getName(), achievement.getGemReward());
									rewardPlayer(player, achievement, rewards);
								}
							}, player, achievement.getName());
						}
					}
					else
					{
						title = C.cPurpleB + "Achievement Level Up! ";

						rewardPlayer(player, achievement, rewards);
					}

					TextComponent message = new TextComponent(title + C.cGray + nameLevel);

					message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(C.cAquaB + nameLevel + "\n" + C.cDAqua + description.toString().substring(1) + " " + reward)
							.create()));

					player.sendMessage("");
					player.spigot().sendMessage(message);
					player.sendMessage("");
					player.playSound(player.getLocation(), Sound.LEVEL_UP, 1, 1);
				}
			});

			manager.clearLog(player);
		}
	}

	private String getAchievementName(Achievement achievement, AchievementData data)
	{
		String nameLevel = achievement.getName();

		if (achievement.getMaxLevel() > 1)
		{
			if (achievement.hasLevelNames())
			{
				String tier = data.getLevel() == 0 ? achievement.getDefaultLevelName() : achievement.getLevelNames()[Math.min(data.getLevel(), achievement.getLevelNames().length) - 1];
				nameLevel += " " + tier;
			}
			else
			{
				nameLevel += " " + (data.getLevel() + (achievement == Achievement.GLOBAL_GEM_HUNTER ? 1 : 0));
			}
		}

		return nameLevel;
	}

	private int[] getRewards(Achievement achievement, int level)
	{
		int gems = 0;
		int crowns = 0;
		int xp = 0;

		if (achievement.getLevelUpRewards().length > 0)
		{
			int[] rewards = achievement.getLevelUpRewards()[Math.min(level, achievement.getLevelUpRewards().length) - 1];
			gems += rewards[0];
			crowns += rewards[1];
			xp += rewards[2];
		}

		return new int[]{gems, crowns, xp};
	}

	private String getRewardsString(Player player, int[] rewards)
	{
		String reward = "";
		int gems = rewards[0];
		int crowns = rewards[1];
		int xp = rewards[2];

		if (gems > 0)
		{
			reward += C.cGreenB + "+" + gems + " Gems ";
		}
		if (crowns > 0)
		{
			reward += C.cGoldB + "+" + crowns + " Crowns ";
		}
		if (xp > 0)
		{
			reward += C.cYellowB + "+" + xp + " EXP ";
		}

		return reward;
	}

	private void rewardPlayer(Player player, Achievement achievement, int[] rewards)
	{
		int gems = rewards[0];
		int crowns = rewards[1];
		int xp = rewards[2];

		if (gems > 0)
		{
			_donationManager.rewardCurrency(GlobalCurrency.GEM, player, achievement.getName(), gems);
		}
		if (crowns > 0)
		{
			_donationManager.rewardCrowns(crowns, player);
		}
		if (xp > 0)
		{
			// Need to delay a tick so we don't CME the achievement log loop
			_statsManager.runSyncLater(() -> _statsManager.incrementStat(player, Achievement.GLOBAL_MINEPLEX_LEVEL.getStats()[0], xp), 0);
		}
	}
}
