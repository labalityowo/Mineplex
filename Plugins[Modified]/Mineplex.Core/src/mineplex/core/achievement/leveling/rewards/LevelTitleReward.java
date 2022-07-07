package mineplex.core.achievement.leveling.rewards;

import org.bukkit.entity.Player;

import mineplex.core.Managers;
import mineplex.core.common.util.C;
import mineplex.core.titles.tracks.Track;
import mineplex.core.titles.tracks.TrackManager;

public class LevelTitleReward implements LevelReward
{

	private static final TrackManager TRACK_MANAGER = Managers.require(TrackManager.class);

	private final Track _track;

	public LevelTitleReward(Track track)
	{
		_track = track;
	}

	@Override
	public void claim(Player player)
	{
		TRACK_MANAGER.unlockTrack(player, _track);
	}

	@Override
	public String getDescription()
	{
		return C.cAqua + _track.getLongName() + C.Reset + " (" + C.cAqua + "Title" + C.Reset + ")";
	}
}
