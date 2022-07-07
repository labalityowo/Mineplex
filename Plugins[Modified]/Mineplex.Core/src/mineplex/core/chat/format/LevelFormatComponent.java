package mineplex.core.chat.format;

import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;

import org.bukkit.entity.Player;

import mineplex.core.achievement.AchievementManager;

public class LevelFormatComponent implements ChatFormatComponent
{

	private final AchievementManager _achievementManager;

	public LevelFormatComponent(AchievementManager achievementManager)
	{
		_achievementManager = achievementManager;
	}

	@Override
	public BaseComponent getText(Player player)
	{
		return new TextComponent(_achievementManager.getMineplexLevel(player));
	}
}
