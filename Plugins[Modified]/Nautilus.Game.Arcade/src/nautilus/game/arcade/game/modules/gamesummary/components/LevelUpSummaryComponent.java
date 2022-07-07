package nautilus.game.arcade.game.modules.gamesummary.components;

import java.util.function.Function;

import org.bukkit.entity.Player;

import mineplex.core.achievement.Achievement;
import mineplex.core.achievement.AchievementData;
import mineplex.core.achievement.AchievementLog;
import mineplex.core.common.Pair;
import mineplex.core.common.util.C;

import nautilus.game.arcade.game.modules.gamesummary.GameSummaryComponentType;

public class LevelUpSummaryComponent extends ExperienceSummaryComponent
{

	public LevelUpSummaryComponent(Function<Player, Pair<AchievementLog, AchievementData>> getFunction)
	{
		super(GameSummaryComponentType.LEVEL_UP, getFunction);
	}

	@Override
	public String getMainText(Pair<AchievementLog, AchievementData> data)
	{
		if (data.getLeft() == null || !data.getLeft().LevelUp)
		{
			return null;
		}

		int level = data.getRight().getLevel();

		return C.cPurpleB + "Level Up! " + Achievement.getExperienceString(level - 1) + C.cGray + " " + DOUBLE_ARROW + " " + Achievement.getExperienceString(level);
	}
}
