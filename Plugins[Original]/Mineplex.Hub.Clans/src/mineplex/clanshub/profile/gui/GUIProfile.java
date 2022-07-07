package mineplex.clanshub.profile.gui;

import mineplex.clanshub.profile.buttons.ButtonPrefs;
import mineplex.clanshub.profile.buttons.ButtonStats;
import mineplex.core.achievement.AchievementManager;
import mineplex.core.gui.SimpleGui;
import mineplex.core.preferences.PreferencesManager;

import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Profile GUI
 */
public class GUIProfile extends SimpleGui
{
	private PreferencesManager _preferencesManager;
	private AchievementManager _achievementManager;
	
	public GUIProfile(Plugin plugin, Player player, PreferencesManager preferencesManager, AchievementManager achievementManager)
	{
		super(plugin, player, "My Profile", 9*3);
		_preferencesManager = preferencesManager;
		_achievementManager = achievementManager;
		
		setItem(12, new ButtonStats(this, player));
		setItem(14, new ButtonPrefs(this, player));	
	}

	public PreferencesManager getPrefManager()
	{
		return _preferencesManager;
	}
	
	public AchievementManager getAchievementManager()
	{
		return _achievementManager;
	}
}