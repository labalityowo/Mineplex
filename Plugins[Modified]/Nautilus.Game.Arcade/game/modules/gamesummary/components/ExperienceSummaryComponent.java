package nautilus.game.arcade.game.modules.gamesummary.components;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import org.bukkit.entity.Player;

import mineplex.core.achievement.AchievementData;
import mineplex.core.achievement.AchievementLog;
import mineplex.core.common.Pair;
import mineplex.core.common.util.C;

import nautilus.game.arcade.game.modules.gamesummary.GameSummaryComponent;
import nautilus.game.arcade.game.modules.gamesummary.GameSummaryComponentType;
import nautilus.game.arcade.stats.ExperienceStatTracker;

public class ExperienceSummaryComponent extends GameSummaryComponent<Pair<AchievementLog, AchievementData>>
{

	public ExperienceSummaryComponent(Function<Player, Pair<AchievementLog, AchievementData>> getFunction)
	{
		this(GameSummaryComponentType.EXPERIENCE, getFunction);
	}

	public ExperienceSummaryComponent(GameSummaryComponentType type, Function<Player, Pair<AchievementLog, AchievementData>> getFunction)
	{
		super(type, getFunction);
	}

	@Override
	public String getMainText(Pair<AchievementLog, AchievementData> data)
	{
		if (data.getLeft() == null)
		{
			return null;
		}

		return C.cGray + "+" + C.cYellow + data.getLeft().Amount + C.cGray + " Experience" + (ExperienceStatTracker.DOUBLE_EXP ? C.cGoldB + " DOUBLE EXP" : "");
	}

	@Override
	public List<String> getHoverText(Pair<AchievementLog, AchievementData> data)
	{
		AchievementData achievementData = data.getRight();

		if (achievementData == null)
		{
			return null;
		}

		List<String> text = new ArrayList<>(2);

		text.add(C.cGray + "You are level " + C.cGreen + achievementData.getLevel());

		if (data.getRight().getExpRemainder() > 0)
		{
			text.add(C.cYellow + (achievementData.getExpNextLevel() - achievementData.getExpRemainder()) + " EXP " + C.cGray + "until next level");
		}

		return text;
	}

	@Override
	public boolean sendMessage(Player player)
	{
		boolean sent = super.sendMessage(player);

		if (!(this instanceof LevelUpSummaryComponent))
		{
			sendBlank(player);
		}

		return sent;
	}
}
