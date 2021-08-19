package nautilus.game.arcade.game.modules.gamesummary.components;

import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.entity.Player;

import mineplex.core.achievement.Achievement;
import mineplex.core.achievement.AchievementData;
import mineplex.core.achievement.AchievementManager;
import mineplex.core.common.currency.GlobalCurrency;
import mineplex.core.common.jsonchat.HoverEvent;
import mineplex.core.common.jsonchat.JsonMessage;
import mineplex.core.common.util.C;
import mineplex.core.donation.DonationManager;
import mineplex.core.stats.StatsManager;
import mineplex.core.task.TaskManager;

import nautilus.game.arcade.ArcadeManager;
import nautilus.game.arcade.game.modules.gamesummary.GameSummaryComponentType;

public class AchievementSummaryComponent extends ComplexSummaryComponent
{

	private final AchievementManager _achievementManager;
	private final DonationManager _donationManager;
	private final StatsManager _statsManager;
	private final TaskManager _taskManager;

	public AchievementSummaryComponent(ArcadeManager manager)
	{
		super(GameSummaryComponentType.ACHIEVEMENT);

		_achievementManager = manager.GetAchievement();
		_donationManager = manager.GetDonation();
		_statsManager = manager.GetStatsManager();
		_taskManager = manager.GetTaskManager();
	}

	@Override
	public boolean sendMessage(Player player)
	{
		AtomicInteger progressFor = new AtomicInteger();
		StringBuilder hoverTextBuilder = new StringBuilder(100);

		_achievementManager.getLog(player).forEach((achievement, log) ->
		{
			AchievementData data = _achievementManager.get(player, achievement);

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

					_taskManager.completedTask(success ->
					{
						if (success)
						{
							_donationManager.rewardCurrencyUntilSuccess(GlobalCurrency.GEM, player, achievement.getName(), achievement.getGemReward());
							rewardPlayer(player, achievement, rewards);
						}
					}, player, achievement.getName());
				}
				else
				{
					title = C.cPurpleB + "Achievement Level Up! ";

					rewardPlayer(player, achievement, rewards);
				}

				if (!reward.isEmpty())
				{
					reward = "\n\n" + reward;
				}

				new JsonMessage(title + C.cGray + nameLevel)
						.hover(HoverEvent.SHOW_TEXT,
								C.cAquaB + nameLevel + "\n" +
										C.cDAqua + description.toString().substring(1) +
										reward
						)
						.sendToPlayer(player);
			}
			else if (!_taskManager.hasCompletedTask(player, achievement.getName()) && data.getExpNextLevel() != -1)
			{
				String nameLevel = getAchievementName(achievement, data);

				StringBuilder description = new StringBuilder();

				for (String line : achievement.getDesc())
				{
					description.append("\n").append(C.cDAqua).append(line);
				}

				hoverTextBuilder
						.append("\n\n")
						.append(C.cAquaB).append(nameLevel)
						.append("\n")
						.append(description.toString().substring(1))
						.append("\n")
						.append(C.cPurple).append(data.getExpRemainder()).append("/").append(data.getExpNextLevel()).append(C.cGray).append(" (+").append(log.Amount).append(")");

				progressFor.getAndIncrement();
			}
		});

		if (progressFor.get() == 0)
		{
			return false;
		}

		new JsonMessage(C.cGray + "Progress for " + C.cYellow + progressFor.get() + " Achievement" + (progressFor.get() == 1 ? "" : "s"))
				.hover(HoverEvent.SHOW_TEXT, hoverTextBuilder.toString().substring(2))
				.sendToPlayer(player);

		sendBlank(player);
		return true;
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

		return new int[] {gems, crowns, xp};
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
		if (xp > 0 && !_taskManager.hasCompletedTask(player, Achievement.GLOBAL_MINEPLEX_LEVEL.getName()))
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
			_statsManager.incrementStat(player, Achievement.GLOBAL_MINEPLEX_LEVEL.getStats()[0], xp);
		}
	}
}
