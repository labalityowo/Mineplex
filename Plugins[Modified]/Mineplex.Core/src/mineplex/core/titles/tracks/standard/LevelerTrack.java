package mineplex.core.titles.tracks.standard;

import net.md_5.bungee.api.ChatColor;

import mineplex.core.Managers;
import mineplex.core.achievement.Achievement;
import mineplex.core.achievement.AchievementManager;
import mineplex.core.titles.tracks.Track;
import mineplex.core.titles.tracks.TrackFormat;
import mineplex.core.titles.tracks.TrackTier;

public class LevelerTrack extends Track
{
	private final AchievementManager _achievementManager = Managers.require(AchievementManager.class);

	public LevelerTrack()
	{
		super("leveler", "Leveler", "This track is unlocked by earning Mineplex levels");
		getRequirements()
				.addTier(new TrackTier(
						"Mineplex Sophomore",
						"Reach level 20",
						player -> (long) _achievementManager.get(player, Achievement.GLOBAL_MINEPLEX_LEVEL).getLevel(),
						20,
						new TrackFormat(ChatColor.GRAY)
				))
				.addTier(new TrackTier(
						"Tree Climber",
						"Reach level 40",
						player -> (long) _achievementManager.get(player, Achievement.GLOBAL_MINEPLEX_LEVEL).getLevel(),
						40,
						new TrackFormat(ChatColor.LIGHT_PURPLE)
				))
				.addTier(new TrackTier(
						"Chiss' Cat",
						"Reach level 60",
						player -> (long) _achievementManager.get(player, Achievement.GLOBAL_MINEPLEX_LEVEL).getLevel(),
						60,
						new TrackFormat(ChatColor.BLUE, null)
				))
				.addTier(new TrackTier(
						"Honorary Guardian",
						"Reach level 80",
						player -> (long) _achievementManager.get(player, Achievement.GLOBAL_MINEPLEX_LEVEL).getLevel(),
						80,
						new TrackFormat(ChatColor.GREEN, null)
				))
				.addTier(new TrackTier(
						"Friend of Douglas",
						"Reach level 100",
						player -> (long) _achievementManager.get(player, Achievement.GLOBAL_MINEPLEX_LEVEL).getLevel(),
						100,
						new TrackFormat(ChatColor.RED, ChatColor.RED)
				));
	}
}
